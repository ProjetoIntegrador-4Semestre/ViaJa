package com.senac.viaja

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnIniciarJornada: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnIniciarJornada = findViewById(R.id.btnIniciarJornada)

        btnIniciarJornada.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("origem", "main")
            startActivity(intent)
        }
    }
}