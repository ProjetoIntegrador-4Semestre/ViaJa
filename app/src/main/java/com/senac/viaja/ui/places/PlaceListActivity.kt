package com.senac.viaja.ui.places

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.adapter.PlaceAdapter
import com.senac.viaja.ui.main.PlaceViewModel

class PlaceListActivity : AppCompatActivity() {

    private lateinit var viewModel: PlaceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val headerTitle = setPageContent(R.layout.activity_place_list, "Lugares", PageTab.EXPLORE) { finish() }

        viewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)

        val query = intent.getStringExtra("q") ?: ""
        val city = intent.getStringExtra("city") ?: ""
        val searchTerm = city.ifBlank { query }
        if (city.isNotBlank()) headerTitle.text = city

        val recycler = findViewById<RecyclerView>(R.id.recycler_places)
        val status = findViewById<android.widget.TextView>(R.id.tvPlaceListStatus)
        status.setOnClickListener { viewModel.searchByCity(searchTerm) }
        recycler.layoutManager = LinearLayoutManager(this)

        var adapter = PlaceAdapter(emptyList()) { place ->
            openPlaceDetails(place.id)
        }
        recycler.adapter = adapter

        viewModel.places.observe(this) { places ->
            adapter = PlaceAdapter(places) { p ->
                openPlaceDetails(p.id)
            }
            recycler.adapter = adapter
            findViewById<android.widget.TextView>(R.id.tvResultsCount).text = "${places.size} locais encontrados"
            status.text = "Nenhum lugar encontrado."
            status.visibility = if (places.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.error.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                status.text = "$message Toque para tentar novamente."
                status.visibility = android.view.View.VISIBLE
                recycler.visibility = android.view.View.GONE
            }
        }

        viewModel.loading.observe(this) { loading ->
            if (loading) {
                status.text = "Carregando lugares…"
                status.visibility = android.view.View.VISIBLE
                recycler.visibility = android.view.View.GONE
            } else {
                recycler.visibility = android.view.View.VISIBLE
            }
        }

        viewModel.searchByCity(searchTerm)
    }

    private fun openPlaceDetails(placeId: String) {
        val intent = Intent(this@PlaceListActivity, PlaceDetailsActivity::class.java)
        intent.putExtra("place_id", placeId)
        startActivity(intent)
    }
}
