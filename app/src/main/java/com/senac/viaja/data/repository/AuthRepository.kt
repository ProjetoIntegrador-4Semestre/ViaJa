package com.senac.viaja.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository(context: Context) {

    private val applicationContext = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("viaja_session", Context.MODE_PRIVATE)
    }

    init {
        ApiClient.AuthSession.tokenProvider = { getToken() }
        ApiClient.AuthSession.onUnauthorized = { rejectedToken ->
            if (prefs.getString("auth_token", null) == rejectedToken) clearSession()
        }
    }

    fun login(
        email: String,
        senha: String,
        onSuccess: (AuthToken) -> Unit,
        onError: (String) -> Unit
    ) {
        ApiClient.service.login(LoginRequest(email, senha)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val user = body.user
                    val rawToken = response.headers()["Authorization"]
                        ?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")
                    if (user == null || user.id <= 0 || user.email.isBlank() || !isTokenCurrent(rawToken)) {
                        onError("Resposta de autenticação inválida. Tente novamente.")
                        return
                    }
                    val token = AuthToken(token = rawToken!!, user = user)
                    saveSession(token)
                    onSuccess(token)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Credenciais inválidas ou erro no servidor"
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onError("Falha na conexão com a API: ${t.localizedMessage}")
            }
        })
    }

    fun register(
        name: String,
        email: String,
        senha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        ApiClient.service.signup(SignupRequest(name, email, senha)).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro ao registrar na API"
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onError("Falha na conexão com a API: ${t.localizedMessage}")
            }
        })
    }

    fun getToken(): String? {
        val token = prefs.getString("auth_token", null)
        if (!isTokenCurrent(token)) {
            clearSession()
            return null
        }
        return token
    }

    fun getUser(): ViajaUser? {
        if (getToken() == null) return null
        val id = prefs.all["user_id"] as? Long ?: return null
        val email = prefs.getString("user_email", null)
        if (id <= 0 || email.isNullOrBlank()) return null
        return ViajaUser(id, email, prefs.getString("user_name", null), prefs.getString("user_mode", null))
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private fun isTokenCurrent(token: String?): Boolean {
        return try {
            val parts = token?.split('.') ?: return false
            if (parts.size != 3) return false
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE), Charsets.UTF_8)
            val claims = org.json.JSONObject(payload)
            claims.getLong("exp") > System.currentTimeMillis() / 1000 && claims.getString("sub").isNotBlank()
        } catch (error: Exception) {
            false
        }
    }

    fun updateStoredUser(name: String, email: String) {
        getUser() ?: return
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
    }

    private fun saveSession(token: AuthToken) {
        prefs.edit()
            .putString("auth_token", token.token)
            .putLong("user_id", token.user.id)
            .putString("user_mode", token.user.defaultMode)
            .putString("user_email", token.user.email)
            .putString("user_name", token.user.name ?: "")
            .apply()
    }
}
