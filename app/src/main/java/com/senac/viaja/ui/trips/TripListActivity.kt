package com.senac.viaja.ui.trips

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.adapter.TripAdapter
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.main.TripViewModel

class TripListActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_trip_list, "Minhas viagens", PageTab.TRIPS, topLevel = true)

        viewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        val userId = AuthRepository(this).getUser()?.id ?: run {
            startActivity(Intent(this, com.senac.viaja.ui.auth.LoginActivity::class.java))
            finish()
            return
        }

        val recycler = findViewById<RecyclerView>(R.id.recycler_trips)
        recycler.layoutManager = LinearLayoutManager(this)

        var adapter = TripAdapter(emptyList()) { trip ->
            openTripDashboard(trip.id ?: "")
        }
        recycler.adapter = adapter

        val status = findViewById<android.widget.TextView>(R.id.tvTripListSubtitle)
        status.setOnClickListener { viewModel.listByUser(userId) }
        viewModel.trips.observe(this) { trips ->
            status.text = if (trips.isEmpty()) "Você ainda não criou viagens." else "${trips.size} viagens"
            adapter = TripAdapter(trips) { t ->
                openTripDashboard(t.id ?: "")
            }
            recycler.adapter = adapter
        }

        viewModel.error.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                status.text = "$message Toque para tentar novamente."
            }
        }

        val btnAddTrip: MaterialButton = findViewById(R.id.btnAddTrip)
        viewModel.loading.observe(this) { loading ->
            btnAddTrip.isEnabled = !loading
            recycler.visibility = if (loading) android.view.View.GONE else android.view.View.VISIBLE
            if (loading) status.text = "Carregando viagens…"
        }
        btnAddTrip.setOnClickListener {
            openTripEditor()
        }
    }

    private val editorResult = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val id = result.data?.getLongExtra(TripEditorActivity.TRIP_ID, -1L) ?: -1L
            if (id > 0) openTripDashboard(id.toString())
        }
    }

    private fun openTripEditor() {
        editorResult.launch(Intent(this, TripEditorActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        val userId = AuthRepository(this).getUser()?.id ?: run { finish(); return }
        if (::viewModel.isInitialized) viewModel.listByUser(userId)
    }

    private fun openTripDashboard(tripId: String) {
        val placeId = intent.getStringExtra("add_place_id")?.toLongOrNull()
        val selectedTripId = tripId.toLongOrNull() ?: return
        if (placeId != null) {
            startActivity(Intent(this, TripItineraryActivity::class.java)
                .putExtra(TripItineraryActivity.TRIP_ID, selectedTripId)
                .putExtra(TripItineraryActivity.ADD_PLACE_ID, placeId))
            return
        }
        val intent = Intent(this@TripListActivity, TripDashboardActivity::class.java)
        intent.putExtra("trip_id", tripId)
        startActivity(intent)
    }
}
