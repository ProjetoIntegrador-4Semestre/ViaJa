package com.senac.viaja.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SubscriptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, PremiumActivity::class.java).putExtra(PremiumActivity.OPEN_SUBSCRIPTION, true))
        finish()
    }
}
