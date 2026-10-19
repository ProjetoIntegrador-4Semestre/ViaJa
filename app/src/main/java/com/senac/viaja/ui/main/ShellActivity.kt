package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.senac.viaja.R
import com.senac.viaja.ui.places.PlaceListActivity
import com.senac.viaja.ui.trips.TripListActivity

class ShellActivity : AppCompatActivity() {

    private lateinit var nav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shell)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nav = findViewById(R.id.bottom_nav)
        setupBottomNav()

        if (savedInstanceState == null) {
            val tab = intent.getStringExtra("tab")
            when (tab) {
                "saved" -> {
                    nav.selectedItemId = R.id.nav_saved
                    navigateToGroup("saved", ::SavedItemsFragment)
                }
                "profile" -> {
                    nav.selectedItemId = R.id.nav_profile
                    navigateToGroup("profile", ::ProfileFragment)
                }
                else -> {
                    nav.selectedItemId = R.id.nav_home
                    navigateToGroup("home", ::HomeFragment)
                }
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
                R.id.nav_home -> navigateToGroup("home", ::HomeFragment)
                R.id.nav_explore -> {
                    startActivity(Intent(this@ShellActivity, ExploreActivity::class.java))
                    true
                }
                R.id.nav_saved -> navigateToGroup("saved", ::SavedItemsFragment)
                R.id.nav_profile -> navigateToGroup("profile", ::ProfileFragment)
                else -> navigateToGroup("home", ::HomeFragment)
            }
        }
    }

    private fun navigateToGroup(group: String, defaultFactory: () -> Fragment): Boolean {
        val savedClassName = NavStateManager.getLastFragmentClassName(this, group)
        val fragment: Fragment = if (savedClassName != null) {
            try {
                Class.forName(savedClassName).getDeclaredConstructor().newInstance() as Fragment
            } catch (e: Exception) {
                defaultFactory()
            }
        } else {
            defaultFactory()
        }

        NavStateManager.saveFragment(this, group, fragment.javaClass.name)

        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, group)
        transaction.commit()
        return true
    }

    fun saveGroupFragment(group: String, fragmentClassName: String) {
        NavStateManager.saveFragment(this, group, fragmentClassName)
    }
}
