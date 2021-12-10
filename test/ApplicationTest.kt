package mychat.room

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import kotlin.test.*
import io.ktor.server.testing.*
import mychat.room.*
import org.junit.Test

class ApplicationTest {
    @Test
    fun testChat() {
        withTestApplication({ module(testing = true) }) {
            handleWebSocketConversation("/chat") { incoming, outgoing ->
                val gson = Gson()
                // connect to chat room
                var receivedFirst =gson.fromJson((incoming.receive() as Frame.Text).readText(),JoinChat::class.java)
                assertEquals("joinChat", receivedFirst.type)
                assertNotNull( receivedFirst.author)
                val myName= receivedFirst.author

                var receivedUpdate =gson.fromJson((incoming.receive() as Frame.Text).readText(),UpdateParticipants::class.java)
                assertEquals("participantsUpdate", receivedUpdate.type)
                assertEquals(myName, receivedUpdate.participants.last())
                assertEquals(1, receivedUpdate.participants.count())


                // chat message send
                var myText = "Hello I am "+myName
                var testMessage = ChatMessage( text= myText, author=myName)
                outgoing.send(Frame.Text(Gson().toJson(testMessage)))
                var receivedMessage =gson.fromJson((incoming.receive() as Frame.Text).readText(),ChatMessage::class.java)
                assertEquals(myName,receivedMessage.author)
                assertEquals(myText,receivedMessage.text)
                assertEquals("message",receivedMessage.type)

                handleWebSocketConversation("/chat"){ other_incoming, other_outgoing ->
                    receivedFirst =gson.fromJson((other_incoming.receive() as Frame.Text).readText(),JoinChat::class.java)
                    var otherName= receivedFirst.author
                    receivedUpdate =gson.fromJson((other_incoming.receive() as Frame.Text).readText(),UpdateParticipants::class.java)
                    assertEquals(2, receivedUpdate.participants.count())
                    assertEquals(setOf(myName,otherName), receivedUpdate.participants)
                    incoming.receive()
                    myText = "Hello I am "+otherName
                    testMessage = ChatMessage( text= myText, author=otherName)
                    other_outgoing.send(Frame.Text(Gson().toJson(testMessage)))
                    receivedMessage =gson.fromJson((incoming.receive() as Frame.Text).readText(),ChatMessage::class.java)
                    assertEquals(otherName,receivedMessage.author)
                    assertEquals("Hello I am "+otherName,receivedMessage.text)

                }

                receivedUpdate =gson.fromJson((incoming.receive() as Frame.Text).readText(),UpdateParticipants::class.java)
//                val test = (incoming.receive() as Frame.Text).readText()
//                assertEquals("f", test)
                assertEquals(myName, receivedUpdate.participants.last())

            }


        }
    }
}


