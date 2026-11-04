package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.senac.viaja.R
import com.senac.viaja.ui.places.PlaceListActivity
import com.senac.viaja.ui.trips.TripListActivity

class ShellActivity : AppCompatActivity() {

    private lateinit var nav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nav = findViewById(R.id.bottom_nav)
        setupBottomNav()

        val tab = intent.getStringExtra("tab")
        when (tab) {
            "saved" -> {
                nav.selectedItemId = R.id.nav_saved
                openSavedItems()
            }
            "profile" -> {
                nav.selectedItemId = R.id.nav_profile
                openProfile()
            }
            else -> {
                nav.selectedItemId = R.id.nav_home
                openHome()
            }
        }
    }

    private fun setupBottomNav() {
        nav.setSelectedItemId(R.id.nav_home)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_trips -> {
                    startActivity(Intent(this@ShellActivity, TripListActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    openHome()
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this@ShellActivity, com.senac.viaja.ui.main.ExploreActivity::class.java))
                    true
                }
                R.id.nav_saved -> {
                    openSavedItems()
                    true
                }
                R.id.nav_profile -> {
                    openProfile()
                    true
                }
                else -> {
                    openHome()
                    true
                }
            }
        }
    }

    private fun openHome() {
        startActivity(Intent(this@ShellActivity, HomeActivity::class.java))
    }

    private fun openSavedItems() {
        startActivity(Intent(this@ShellActivity, SavedItemsActivity::class.java))
    }

    private fun openProfile() {
        startActivity(Intent(this@ShellActivity, ProfileActivity::class.java))
    }
}
