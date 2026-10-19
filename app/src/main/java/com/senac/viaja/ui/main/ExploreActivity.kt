package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.ui.places.AiTripChatActivity
import com.senac.viaja.ui.places.PlaceListActivity
import com.senac.viaja.ui.profile.EditProfileActivity
import com.senac.viaja.ui.trips.TripListActivity

class ExploreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNav()
        setupListeners()
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav?.selectedItemId = R.id.nav_explore
        nav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, ShellActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_explore -> true
                R.id.nav_trips -> {
                    startActivity(Intent(this, TripListActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_saved -> {
                    startActivity(Intent(this, ShellActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ShellActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        val btnSettings = findViewById<View>(R.id.btnExploreSettings)
        val btnAiChat = findViewById<MaterialButton>(R.id.btnExploreAiChat)
        val etSearch = findViewById<TextInputEditText>(R.id.etExploreSearch)
        val btnFilter = findViewById<MaterialButton>(R.id.btnExploreFilter)

        val cardRio = findViewById<View>(R.id.cardExploreRio)
        val btnRioAction = findViewById<MaterialButton>(R.id.btnExploreRioAction)
        val cardSp = findViewById<View>(R.id.cardExploreSp)
        val cardAmazonas = findViewById<View>(R.id.cardExploreAmazonas)
        val cardMinas = findViewById<View>(R.id.cardExploreMinas)

        btnSettings.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnAiChat.setOnClickListener {
            val intent = Intent(this, AiTripChatActivity::class.java)
            intent.putExtra("trip_name", "Exploração")
            intent.putExtra("trip_mode", "Turismo")
            startActivity(intent)
        }

        val performSearch = {
            val q = etSearch.text?.toString()?.trim() ?: ""
            val intent = Intent(this, PlaceListActivity::class.java)
            intent.putExtra("q", q)
            startActivity(intent)
        }

        btnFilter.setOnClickListener { performSearch() }

        val openCity = { cityName: String ->
            val intent = Intent(this, PlaceListActivity::class.java)
            intent.putExtra("q", cityName)
            startActivity(intent)
        }

        cardRio.setOnClickListener { openCity("Rio de Janeiro") }
        btnRioAction.setOnClickListener { openCity("Rio de Janeiro") }
        cardSp.setOnClickListener { openCity("São Paulo") }
        cardAmazonas.setOnClickListener { openCity("Amazonas") }
        cardMinas.setOnClickListener { openCity("Minas Gerais") }
    }
}
