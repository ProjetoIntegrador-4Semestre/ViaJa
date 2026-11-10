package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.adapter.CityAdapter
import com.senac.viaja.data.model.City
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.places.PlaceListActivity
import com.senac.viaja.ui.main.SavedViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: CityViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        findViewById<TextView>(R.id.btnOpenProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        viewModel = ViewModelProvider(this).get(CityViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        val tvGreeting = findViewById<TextView>(R.id.tvHomeGreeting)
        val etSearch = findViewById<TextInputEditText>(R.id.etHomeSearch)
        val btnExplore = findViewById<MaterialButton>(R.id.btnExploreHome)
        val recycler = findViewById<RecyclerView>(R.id.recycler_destinations)

        val storedUser = authViewModel.getStoredUser()
        tvGreeting.text = if (storedUser?.name != null) "Ol\u00e1, ${storedUser.name}!" else "Ol\u00e1, Viajante!"

        var adapter = CityAdapter(emptyList()) { city ->
            openPlaceList(city.name ?: "")
        }
        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = adapter

        viewModel.cities.observe(this) { cities ->
            adapter = CityAdapter(cities) { c ->
                openPlaceList(c.name ?: "")
            }
            recycler.adapter = adapter
        }

        btnExplore.setOnClickListener {
            val q = etSearch.text?.toString()?.trim()?.ifEmpty { "" } ?: ""
            openPlaceList(q)
        }

        viewModel.list()
    }

    private fun openPlaceList(query: String) {
        val intent = Intent(this, PlaceListActivity::class.java)
        intent.putExtra("q", query)
        startActivity(intent)
    }
}
