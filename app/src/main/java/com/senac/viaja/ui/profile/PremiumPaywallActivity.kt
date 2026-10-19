package com.senac.viaja.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.senac.viaja.R
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.profile.EditProfileActivity

class PremiumPaywallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_premium_paywall)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sub = MockDataSource.subscription

        findViewById<TextView>(R.id.tvPaywallPrice)?.text = "$${sub.amount?.toString() ?: "9,99"} /mês, cobrança anual"

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSubscribeNow)
            ?.setOnClickListener {
                Toast.makeText(this, "Plano ativado (simulado)", android.widget.Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SubscriptionActivity::class.java)
                startActivity(intent)
            }
    }
}
