package com.senac.viaja.ui.profile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.senac.viaja.R
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.data.mock.MockDataSource

class SubscriptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_subscription, "Assinatura", PageTab.PROFILE) { finish() }

        val sub = MockDataSource.subscription

        findViewById<TextView>(R.id.tv_subscription_status_text)?.apply {
            text = if (sub.status == "ativo") "ATIVO" else "INATIVO"
        }
        findViewById<TextView>(R.id.tv_subscription_amount)?.text = "${sub.amount?.toString() ?: "29,90"}"
        findViewById<TextView>(R.id.tv_subscription_next_billing)?.text = sub.expiresAt ?: ""
        findViewById<TextView>(R.id.tv_subscription_cycle)?.text = "Mensal"
    }
}
