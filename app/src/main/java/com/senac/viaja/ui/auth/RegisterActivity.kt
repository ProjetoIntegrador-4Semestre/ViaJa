package com.senac.viaja.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
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
import com.senac.viaja.ui.auth.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNome: TextInputEditText
    private lateinit var etEmailReg: TextInputEditText
    private lateinit var etSenhaReg: TextInputEditText
    private lateinit var cbTermos: CheckBox
    private lateinit var btnCriarContaReg: Button
    private lateinit var btnGoogleReg: Button
    private lateinit var btnVoltar: ImageButton
    private lateinit var tvEntrarLink: TextView
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        etNome = findViewById(R.id.etNome)
        etEmailReg = findViewById(R.id.etEmailReg)
        etSenhaReg = findViewById(R.id.etSenhaReg)
        cbTermos = findViewById(R.id.cbTermos)
        btnCriarContaReg = findViewById(R.id.btnCriarContaReg)
        btnGoogleReg = findViewById(R.id.btnGoogleReg)
        btnVoltar = findViewById(R.id.btnVoltar)
        tvEntrarLink = findViewById(R.id.tvEntrarLink)

        viewModel.user.observe(this) { user ->
            if (user != null) {
                Toast.makeText(this@RegisterActivity, "Conta criada! Faça login.", Toast.LENGTH_LONG).show()
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.putExtra("email", user.email)
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
            }
        }

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

        viewModel.register(nome, email, password)
    }
}
