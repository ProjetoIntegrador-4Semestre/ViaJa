package com.senac.viaja

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class WelcomeActivity : AppCompatActivity() {

    private lateinit var btnEntrar: Button
    private lateinit var btnCriarConta: Button
    private lateinit var btnGoogle: MaterialCardView
    private lateinit var tvVisitante: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

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
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("modo", "visitante")
            startActivity(intent)
            finish()
        }
    }
}