package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.senac.viaja.R
import com.senac.viaja.adapter.SavedAdapter
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.places.PlaceDetailsActivity
import com.senac.viaja.ui.trips.TripListActivity

class SavedItemsActivity : AppCompatActivity() {

    private lateinit var viewModel: SavedViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_items)

        viewModel = ViewModelProvider(this).get(SavedViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        val recycler = findViewById<RecyclerView>(R.id.recycler_saved)

        val status = findViewById<android.widget.TextView>(R.id.tvSavedStatus)

        val storedUser = authViewModel.getStoredUser()
        if (storedUser == null) {
            startActivity(Intent(this, com.senac.viaja.ui.auth.LoginActivity::class.java))
            finish()
            return
        }

        var adapter = SavedAdapter(emptyList()) { item ->
            openPlaceDetails(item.localId)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.saved.observe(this) { items ->
            adapter = SavedAdapter(items) { item ->
                openPlaceDetails(item.localId)
            }
            recycler.adapter = adapter
            status.text = "Você ainda não salvou nenhum lugar."
            status.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.error.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                status.text = "$message Toque para tentar novamente."
                status.visibility = android.view.View.VISIBLE
                recycler.visibility = android.view.View.GONE
            }
        }

        status.setOnClickListener { authViewModel.getStoredUser()?.let { viewModel.list(it.id) } }
        viewModel.loading.observe(this) { loading ->
            if (loading) {
                status.text = "Carregando salvos…"
                status.visibility = android.view.View.VISIBLE
                recycler.visibility = android.view.View.GONE
            } else recycler.visibility = android.view.View.VISIBLE
        }
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        authViewModel.getStoredUser()?.let { viewModel.list(it.id) }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav?.selectedItemId = R.id.nav_saved
        nav?.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_saved) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_trips -> {
                    startActivity(Intent(this, TripListActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun openPlaceDetails(placeId: String) {
        val intent = Intent(this, PlaceDetailsActivity::class.java)
        intent.putExtra("place_id", placeId)
        startActivity(intent)
    }
}
