package com.senac.viaja.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senac.viaja.R
import com.senac.viaja.ui.main.setPageContent

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_forgot_password, "Recuperar senha", null) { finish() }
    }
}