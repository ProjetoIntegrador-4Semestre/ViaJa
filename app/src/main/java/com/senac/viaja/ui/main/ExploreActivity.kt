package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.adapter.CityAdapter
import com.senac.viaja.ui.places.PlaceListActivity

class ExploreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_explore, "Explorar", PageTab.EXPLORE, topLevel = true)

        val search = findViewById<TextInputEditText>(R.id.etExploreSearch)
        findViewById<View>(R.id.btnExploreSearch).setOnClickListener {
            openPlaces("q", search.text?.toString().orEmpty().trim())
        }

        val viewModel = ViewModelProvider(this)[CityViewModel::class.java]
        val list = findViewById<RecyclerView>(R.id.exploreCities)
        val status = findViewById<TextView>(R.id.exploreCityStatus)
        val retry = findViewById<View>(R.id.exploreRetry)
        list.layoutManager = LinearLayoutManager(this)
        retry.setOnClickListener { viewModel.list() }

        fun render() {
            val cities = viewModel.cities.value.orEmpty()
            val loading = viewModel.loading.value == true
            val error = viewModel.error.value
            list.isVisible = !loading && error == null && cities.isNotEmpty()
            retry.isVisible = !loading && error != null
            status.isVisible = !list.isVisible
            status.text = when {
                loading -> "Carregando destinos…"
                error != null -> error
                else -> "Nenhum destino disponível."
            }
        }

        viewModel.cities.observe(this) { cities ->
            list.adapter = CityAdapter(cities) { city ->
                city.name?.let { openPlaces("city", it) }
            }
            render()
        }
        viewModel.loading.observe(this) { render() }
        viewModel.error.observe(this) { render() }
        if (viewModel.cities.value == null && viewModel.loading.value != true) viewModel.list()
    }

    private fun openPlaces(key: String, value: String) {
        startActivity(Intent(this, PlaceListActivity::class.java).putExtra(key, value))
    }
}
