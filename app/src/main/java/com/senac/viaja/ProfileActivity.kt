package com.senac.viaja

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<TextView>(R.id.btnProfileBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnEditProfile).setOnClickListener { showNotice("Edição de perfil") }
        findViewById<TextView>(R.id.btnEditInterests).setOnClickListener { showNotice("Edição de interesses") }
        findViewById<TextView>(R.id.btnAddInterest).setOnClickListener { showNotice("Adicionar interesse") }
        findViewById<TextView>(R.id.btnManageSubscription).setOnClickListener { startActivity(Intent(this, PremiumActivity::class.java)) }
    }

    private fun showNotice(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
