package com.senac.viaja.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.senac.viaja.data.model.AuthToken
import com.senac.viaja.data.model.LoginRequest
import com.senac.viaja.data.model.LoginResponse
import com.senac.viaja.data.model.SignupRequest
import com.senac.viaja.data.model.ApiResponse
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.data.remote.ApiClient

class AuthRepository(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("viaja_session", Context.MODE_PRIVATE)
    }

    fun login(
        email: String,
        senha: String,
        onSuccess: (AuthToken) -> Unit,
        onError: (String) -> Unit
    ) {
        // Simula login via API: qualquer e-mail/senha válidos entram com o usuário mock
        if (email.isNotEmpty() && senha.length >= 6) {
            val user = MockDataSource.currentUser.copy(email = email)
            val token = AuthToken(token = "mock-token-${user.id}", user = user)
            ApiClient.AuthSession.token = token.token
            saveSession(token)
            onSuccess(token)
        } else {
            onError("Credenciais inválidas")
        }
    }

    fun register(
        name: String,
        email: String,
        senha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isNotEmpty() && email.isNotEmpty() && senha.length >= 6) {
            onSuccess()
        } else {
            onError("Preencha todos os campos corretamente")
        }
    }

    fun getToken(): String? {
        return ApiClient.AuthSession.token ?: prefs.getString("auth_token", null)
    }

    fun getUser(): ViajaUser? {
        val id = prefs.getInt("user_id", -1).toLong()
        val email = prefs.getString("user_email", null)
        val name = prefs.getString("user_name", null)
        return if (id == -1L || email == null) null else ViajaUser(id, email, name)
    }

    fun clearSession() {
        ApiClient.AuthSession.token = null
        prefs.edit().clear().apply()
    }

    fun updateStoredUser(nome: String, email: String) {
        val user = getUser() ?: return
        prefs.edit()
            .putString("user_name", nome)
            .putString("user_email", email)
            .apply()
    }

    private fun saveSession(token: AuthToken) {
        prefs.edit()
            .putString("auth_token", token.token)
            .putInt("user_id", token.user.id.toInt())
            .putString("user_email", token.user.email)
            .putString("user_name", token.user.name ?: "")
            .apply()
    }
}
