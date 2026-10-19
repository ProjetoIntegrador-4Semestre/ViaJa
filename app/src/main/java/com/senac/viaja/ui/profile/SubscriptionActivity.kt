package com.senac.viaja.ui.profile

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.senac.viaja.R
import com.senac.viaja.data.mock.MockDataSource

class SubscriptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_subscription)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sub = MockDataSource.subscription

        findViewById<TextView>(R.id.tv_subscription_status_text)?.apply {
            text = if (sub.status == "ativo") "ATIVO" else "INATIVO"
        }
        findViewById<TextView>(R.id.tv_subscription_amount)?.text = "${sub.amount?.toString() ?: "29,90"}"
        findViewById<TextView>(R.id.tv_subscription_next_billing)?.text = sub.expiresAt ?: ""
        findViewById<TextView>(R.id.tv_subscription_cycle)?.text = "Mensal"
    }
}
