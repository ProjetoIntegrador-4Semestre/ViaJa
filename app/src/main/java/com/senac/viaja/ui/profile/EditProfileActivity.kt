package com.senac.viaja.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.main.UserViewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_edit_profile, "Editar perfil", PageTab.PROFILE) { finish() }

        etName = findViewById(R.id.etEditName)
        etEmail = findViewById(R.id.etEditEmail)
        btnSave = findViewById(R.id.btnSaveProfile)

        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        val user = authViewModel.getStoredUser()
        if (user == null) {
            finish()
            return
        }
        etName.setText(user.name)
        etEmail.setText(user.email)

        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Preencha nome e e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userViewModel.update(user.id, name, email, { finish() }) { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
