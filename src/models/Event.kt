package mychat.room.models



abstract class Event(val type: String)
data class ChatMessage(val text: String = "", val author: String = "") : Event("message")
data class JoinChat(val previousMessages: ArrayList<ChatMessage>, val author: String) : Event("joinChat")
data class UpdateParticipants(val participants: Set<String>) : Event("participantsUpdate")
