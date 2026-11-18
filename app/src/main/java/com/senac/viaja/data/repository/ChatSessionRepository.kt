package com.senac.viaja.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.senac.viaja.data.model.ChatSession

class ChatSessionRepository(context: Context, private val userId: Long?) {
    private val prefs = context.applicationContext.getSharedPreferences("vi_conversations", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): MutableList<ChatSession> {
        val owner = userId ?: return mutableListOf()
        val raw = prefs.getString("sessions_$owner", null) ?: return mutableListOf()
        return try {
            gson.fromJson<MutableList<ChatSession>>(raw, object : TypeToken<MutableList<ChatSession>>() {}.type)
                ?: mutableListOf()
        } catch (_: com.google.gson.JsonParseException) {
            mutableListOf()
        }
    }

    fun save(sessions: List<ChatSession>) {
        val owner = userId ?: return
        prefs.edit().putString("sessions_$owner", gson.toJson(sessions)).apply()
    }
}
