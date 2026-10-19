package com.senac.viaja.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.auth.AuthViewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etName = findViewById(R.id.etEditName)
        etEmail = findViewById(R.id.etEditEmail)
        etPhone = findViewById(R.id.etEditPhone)
        etBio = findViewById(R.id.etEditBio)
        btnSave = findViewById(R.id.btnSaveProfile)

        // Preenche com dados do usuário logado (vem da API/mock)
        val user = MockDataSource.currentUser
        etName.setText(user.name ?: "")
        etEmail.setText(user.email ?: "")
        etPhone.setText("(11) 98765-4321")
        etBio.setText("")

        val authViewModel = AuthViewModel(application)

        btnSave.setOnClickListener {
            val nome = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            if (nome.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Preencha nome e e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Simula atualização via API
            Toast.makeText(this, "Perfil atualizado com sucesso", Toast.LENGTH_SHORT).show()
            // Atualiza o usuário salvo na sessão
            authViewModel.updateStoredUser(nome, email)
            val intent = Intent()
            intent.putExtra("nome", nome)
            intent.putExtra("email", email)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}
