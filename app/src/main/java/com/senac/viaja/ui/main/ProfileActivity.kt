package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.senac.viaja.R
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.auth.WelcomeActivity
import com.senac.viaja.ui.profile.EditProfileActivity
import com.senac.viaja.ui.profile.PremiumActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_profile, "Perfil", PageTab.PROFILE, topLevel = true) {
            if (isTaskRoot) startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        val storedUser = authViewModel.getStoredUser()
        showAccount(storedUser)
        userViewModel.user.observe(this, ::showAccount)
        storedUser?.let { userViewModel.getById(it.id) }

        findViewById<View>(R.id.btnProfileLogout).apply {
            isVisible = storedUser != null
            setOnClickListener {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("Sair da conta?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sair") { _, _ ->
                        authViewModel.logout()
                        startActivity(Intent(this@ProfileActivity, WelcomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    }
                    .show()
            }
        }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            val destination = if (authViewModel.getStoredUser() == null) {
                LoginActivity::class.java
            } else {
                EditProfileActivity::class.java
            }
            startActivity(Intent(this, destination))
        }
        findViewById<View>(R.id.btnEditInterests).setOnClickListener { showInterestNotice() }
        findViewById<View>(R.id.btnAddInterest).setOnClickListener { showInterestNotice() }
        findViewById<View>(R.id.btnManageSubscription).setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }
    }

    private fun showAccount(user: ViajaUser?) {
        findViewById<TextView>(R.id.tvProfileName).text = user?.name ?: "Viajante"
        val email = user?.email ?: "Não informado"
        findViewById<TextView>(R.id.tvProfileEmail).text = "Endereço de e-mail\n$email"
        val status = if (user == null) "Visitante" else "Sessão iniciada"
        findViewById<TextView>(R.id.tvProfileStatus).text = "Status da conta\n$status"
    }

    private fun showInterestNotice() {
        Toast.makeText(this, "A edição de interesses ainda não está disponível.", Toast.LENGTH_SHORT).show()
    }
}
