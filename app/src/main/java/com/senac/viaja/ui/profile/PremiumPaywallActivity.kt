package com.senac.viaja.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senac.viaja.R
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.data.mock.MockDataSource

class PremiumPaywallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_premium_paywall, "Viajá Premium", PageTab.PROFILE) { finish() }

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
