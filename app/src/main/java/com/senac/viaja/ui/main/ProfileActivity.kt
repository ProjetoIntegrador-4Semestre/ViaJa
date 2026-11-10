package com.senac.viaja.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.senac.viaja.R
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.profile.EditProfileActivity
import com.senac.viaja.ui.profile.PremiumActivity
import com.senac.viaja.ui.trips.TripListActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        val storedUser = authViewModel.getStoredUser()
        showAccount(storedUser)
        userViewModel.user.observe(this, ::showAccount)
        storedUser?.let { userViewModel.getById(it.id) }

        findViewById<View>(R.id.btnProfileBack).setOnClickListener {
            if (isTaskRoot) startActivity(Intent(this, HomeActivity::class.java))
            finish()
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

        openTab(R.id.navHome, HomeActivity::class.java)
        openTab(R.id.navExplore, ExploreActivity::class.java)
        openTab(R.id.navTrips, TripListActivity::class.java)
        openTab(R.id.navSaved, SavedItemsActivity::class.java)
    }

    private fun showAccount(user: ViajaUser?) {
        findViewById<TextView>(R.id.tvProfileName).text = user?.name ?: "Viajante"
        val email = user?.email ?: "Não informado"
        findViewById<TextView>(R.id.tvProfileEmail).text = "Endereço de e-mail\n$email"
        val status = if (user == null) "Visitante" else "Sessão iniciada"
        findViewById<TextView>(R.id.tvProfileStatus).text = "Status da conta\n$status"
    }

    private fun openTab(viewId: Int, destination: Class<out Activity>) {
        findViewById<View>(viewId).setOnClickListener {
            startActivity(Intent(this, destination).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    private fun showInterestNotice() {
        Toast.makeText(this, "A edição de interesses ainda não está disponível.", Toast.LENGTH_SHORT).show()
    }
}
