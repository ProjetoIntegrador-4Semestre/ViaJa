package com.senac.viaja

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etSenha: TextInputEditText
    private lateinit var btnVoltar: ImageButton
    private lateinit var tvEsqueciSenha: TextView
    private lateinit var btnEntrarLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tvCriarAgoraLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        btnVoltar = findViewById(R.id.btnVoltar)
        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha)
        btnEntrarLogin = findViewById(R.id.btnEntrarLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tvCriarAgoraLink = findViewById(R.id.tvCriarAgoraLink)

        etEmail.setText(intent.getStringExtra("email") ?: "")

        btnEntrarLogin.setOnClickListener {
            blockLogin()
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        tvEsqueciSenha.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        tvCriarAgoraLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Login com Google em breve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun blockLogin() {
        val email = etEmail.text.toString().trim()
        val password = etSenha.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://viaja-api.onrender.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.login(LoginRequest(email, password))
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("email", body.user?.email ?: email)
                    intent.putExtra("origem", "login")
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, body?.message ?: "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Erro de rede: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class ViajaUser(
        val id: Int,
        val email: String
    )

    data class LoginResponse(
        val status: String,
        val message: String,
        val user: ViajaUser?
    )

    interface ApiService {
        @POST("login.php")
        fun login(@Body body: LoginRequest): Call<LoginResponse>
    }
}