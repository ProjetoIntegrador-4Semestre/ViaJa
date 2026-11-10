package com.senac.viaja.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.senac.viaja.R
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.main.HomeActivity
import com.senac.viaja.ui.main.MainActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var btnEntrar: Button
    private lateinit var btnCriarConta: Button
    private lateinit var btnGoogle: MaterialCardView
    private lateinit var tvVisitante: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login se já houver sessão salva
        val authRepo = AuthRepository(this)
        if (authRepo.getUser() != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnEntrar = findViewById(R.id.btnEntrar)
        btnCriarConta = findViewById(R.id.btnCriarConta)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvVisitante = findViewById(R.id.tvVisitante)

        btnEntrar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("origem", "welcome")
            startActivity(intent)
        }

        btnCriarConta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("origem", "welcome")
            startActivity(intent)
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Login com Google em breve", Toast.LENGTH_SHORT).show()
        }

        tvVisitante.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("modo", "visitante")
            startActivity(intent)
            finish()
        }
    }
}
