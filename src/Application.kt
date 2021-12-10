package mychat.room

import io.ktor.application.*
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CORS
import io.ktor.http.*
import java.io.File
import java.time.*
import kotlin.collections.ArrayList
import mychat.room.routes.registerEventRoutes


abstract class Event(val type: String)
data class ChatMessage(val text: String = "", val author: String = "") : Event("message")
data class JoinChat(val previousMessages: ArrayList<ChatMessage>, val author: String) : Event("joinChat")
data class UpdateParticipants(val participants: Set<String>) : Event("participantsUpdate")


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    // Uncomment this if you are doing local development
    install(CORS) {
        header(HttpHeaders.AccessControlAllowOrigin)
        anyHost()
    }


    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(AutoHeadResponse)
    registerEventRoutes()
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
