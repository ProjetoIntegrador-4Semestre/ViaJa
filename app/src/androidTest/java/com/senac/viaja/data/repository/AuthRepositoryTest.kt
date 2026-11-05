package com.senac.viaja.data.repository

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.senac.viaja.data.remote.ApiClient
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {
    @Test
    fun restoresValidSessionAndRejectsExpiredOrSimulatedTokens() {
        val context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                baseContext.getSharedPreferences("session_repository_test", mode)
        }
        val prefs = context.getSharedPreferences("viaja_session", Context.MODE_PRIVATE)
        val repository = AuthRepository(context)
        try {
            prefs.edit().clear().putString("auth_token", "bearer-token").commit()
            assertNull(repository.getUser())
            assertFalse(prefs.contains("auth_token"))

            val payload = Base64.encodeToString(
                "{\"exp\":${System.currentTimeMillis() / 1000 + 3600},\"sub\":\"session@example.com\"}".toByteArray(),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val token = "header.$payload.signature"
            val userId = Int.MAX_VALUE.toLong() + 1
            prefs.edit().putString("auth_token", token).putLong("user_id", userId)
                .putString("user_email", "session@example.com").putString("user_name", "Session User")
                .putString("user_mode", "work").commit()
            assertEquals(userId, repository.getUser()?.id)
            assertEquals("work", repository.getUser()?.defaultMode)
            assertEquals(token, AuthRepository(context).getToken())
            assertEquals(token, ApiClient.AuthSession.tokenProvider?.invoke())
            ApiClient.AuthSession.onUnauthorized?.invoke("previous-token")
            assertEquals(token, repository.getToken())
            ApiClient.AuthSession.onUnauthorized?.invoke(token)
            assertNull(repository.getUser())

            val expired = Base64.encodeToString("{\"exp\":1,\"sub\":\"session@example.com\"}".toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
            prefs.edit().putString("auth_token", "header.$expired.signature").commit()
            assertNull(repository.getToken())
            assertFalse(prefs.contains("auth_token"))
        } finally {
            repository.clearSession()
            ApiClient.AuthSession.tokenProvider = null
            ApiClient.AuthSession.onUnauthorized = null
        }
    }
}
