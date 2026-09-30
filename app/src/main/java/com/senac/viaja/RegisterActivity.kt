package com.senac.viaja

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
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

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNome: TextInputEditText
    private lateinit var etEmailReg: TextInputEditText
    private lateinit var etSenhaReg: TextInputEditText
    private lateinit var cbTermos: CheckBox
    private lateinit var btnCriarContaReg: Button
    private lateinit var btnGoogleReg: Button
    private lateinit var btnVoltar: ImageButton
    private lateinit var tvEntrarLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etNome = findViewById(R.id.etNome)
        etEmailReg = findViewById(R.id.etEmailReg)
        etSenhaReg = findViewById(R.id.etSenhaReg)
        cbTermos = findViewById(R.id.cbTermos)
        btnCriarContaReg = findViewById(R.id.btnCriarContaReg)
        btnGoogleReg = findViewById(R.id.btnGoogleReg)
        btnVoltar = findViewById(R.id.btnVoltar)
        tvEntrarLink = findViewById(R.id.tvEntrarLink)

        btnCriarContaReg.setOnClickListener {
            blockRegister()
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        tvEntrarLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnGoogleReg.setOnClickListener {
            Toast.makeText(this, "Cadastro com Google em breve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun blockRegister() {
        val nome = etNome.text.toString().trim()
        val email = etEmailReg.text.toString().trim()
        val password = etSenhaReg.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Digite um e-mail válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8 || !password.any { it.isDigit() } || !password.any { it.isUpperCase() }) {
            Toast.makeText(this, "A senha precisa de 8 caracteres, 1 número e 1 letra maiúscula", Toast.LENGTH_LONG).show()
            return
        }

        if (!cbTermos.isChecked) {
            Toast.makeText(this, "Aceite os Termos e Privacidade", Toast.LENGTH_SHORT).show()
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

        val call = apiService.signup(SignupRequest(nome, email, password))
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    Toast.makeText(this@RegisterActivity, "Conta criada! Faça login.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, body?.message ?: "Erro no cadastro", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Erro de rede: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    data class SignupRequest(
        val name: String,
        val email: String,
        val password: String
    )

    data class ApiResponse(
        val status: String,
        val message: String
    )

    interface ApiService {
        @POST("signup.php")
        fun signup(@Body body: SignupRequest): Call<ApiResponse>
    }
}