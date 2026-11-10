package com.senac.viaja.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.main.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etSenha: TextInputEditText
    private lateinit var btnVoltar: ImageButton
    private lateinit var tvEsqueciSenha: TextView
    private lateinit var btnEntrarLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tvCriarAgoraLink: TextView
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        btnVoltar = findViewById(R.id.btnVoltar)
        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha)
        btnEntrarLogin = findViewById(R.id.btnEntrarLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tvCriarAgoraLink = findViewById(R.id.tvCriarAgoraLink)

        etEmail.setText(intent.getStringExtra("email"))

        viewModel.user.observe(this) { user ->
            if (user != null) {
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("email", user.email)
                intent.putExtra("origem", "login")
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }
        }

        btnEntrarLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etSenha.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
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
}
