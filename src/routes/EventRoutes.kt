package mychat.room.routes
import io.ktor.application.Application
import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet
import io.ktor.routing.*
import mychat.room.models.*

fun Application.registerEventRoutes() {
    routing {
        EventRoutes()
    }
}


val animals = File("src/animals.txt").readLines()
val colors = File("src/colors.txt").readLines()

fun Route.EventRoutes() {

        val previousMessages: ArrayList<ChatMessage> = ArrayList()
        val connections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())
        val authors = Collections.synchronizedSet(LinkedHashSet<String>())


        static("/static") {
            resources("client/build/static")
        }

        static("/") {
            resources("client/build/")
            default("client/build/index.html")
        }

        webSocket("/chat") {
            connections.add(this)
            println("A new socket has joined the chat!")
            val author = colors.random() + " " + animals.random()

            outgoing.send(Frame.Text(Gson().toJson(JoinChat(ArrayList<ChatMessage>(), author))))
            authors.add(author)
            connections.forEach { socket -> socket.outgoing.send(Frame.Text(Gson().toJson(UpdateParticipants(authors)))) }
            try {
                while (true) {
                    val frame = incoming.receive()
                    when (frame) {
                        is Frame.Text -> {
                            val json = frame.readText()
                            val chatMessage = Gson().fromJson(json, ChatMessage::class.java)
                            println("New Chat Message received: $json")
                            previousMessages.add(chatMessage)
                            for (socket in connections) {
                                socket.outgoing.send(Frame.Text(Gson().toJson(chatMessage)))
                            }
                        }
                    }
                }
            } catch (exception: ClosedReceiveChannelException) {
                println("A socket has left the chat!")
            } finally {
                authors.remove(author)
                connections.remove(this)
                connections.forEach { socket -> socket.outgoing.send(Frame.Text(Gson().toJson(UpdateParticipants(authors)))) }

            }
        }
}