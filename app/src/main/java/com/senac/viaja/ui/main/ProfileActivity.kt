package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.profile.EditProfileActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        val tvName = findViewById<TextView>(R.id.tv_profile_name)

        val storedUser = authViewModel.getStoredUser()
        tvName.text = storedUser?.name ?: "Usu\u00e1rio"
        storedUser?.let { userViewModel.getById(it.id) }

        userViewModel.user.observe(this) { user ->
            tvName.text = user.name ?: "Usu\u00e1rio"
        }

        val btnEditProfile = findViewById<MaterialButton>(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
