package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.senac.viaja.R
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.trips.TripListActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setSelectedItemId(R.id.nav_home)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_explore -> {
                    startActivity(Intent(this@MainActivity, ShellActivity::class.java))
                    true
                }
                R.id.nav_trips -> {
                    startActivity(Intent(this@MainActivity, TripListActivity::class.java))
                    true
                }
                R.id.nav_saved -> {
                    startActivity(Intent(this@MainActivity, ShellActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this@MainActivity, ShellActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
