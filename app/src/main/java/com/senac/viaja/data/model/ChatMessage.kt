package com.senac.viaja.data.model

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val delivery: String = "local",
    val requestId: String? = null
)

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "Nova conversa",
    val tripId: Long? = null,
    val tripName: String = "",
    var mode: String = "tourism",
    var pace: String = "Equilibrado",
    var draft: String = "",
    val messages: MutableList<ChatMessage> = mutableListOf(),
    var pending: AiMessageWrite? = null,
    var updatedAt: Long = System.currentTimeMillis()
)
