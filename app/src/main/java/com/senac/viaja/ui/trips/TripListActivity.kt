package com.senac.viaja.ui.trips

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import com.senac.viaja.data.repository.CityRepository
import com.senac.viaja.data.repository.TripRepository
import com.senac.viaja.ui.main.TripViewModel

class TripListActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
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

        viewModel.trip.observe(this) { trip ->
            Toast.makeText(this, "Viagem criada", Toast.LENGTH_SHORT).show()
            viewModel.listByUser(userId)
        }
        viewModel.listByUser(userId)

        val btnAddTrip: MaterialButton = findViewById(R.id.btnAddTrip)
        viewModel.loading.observe(this) { loading ->
            btnAddTrip.isEnabled = !loading
            recycler.visibility = if (loading) android.view.View.GONE else android.view.View.VISIBLE
            if (loading) status.text = "Carregando viagens…"
        }
        btnAddTrip.setOnClickListener {
            showCreateTripDialog()
        }
    }

    private fun showCreateTripDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_trip, null)
        val etName: EditText = dialogView.findViewById(R.id.etTripName)
        val citySpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerTripCity)
        val modeSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerTripMode)
        modeSpinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Turismo", "Trabalho"))
        var cities = emptyList<com.senac.viaja.data.model.City>()
        val btnConfirm: Button = dialogView.findViewById(R.id.btnConfirmCreateTrip)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancelCreateTrip)

        val alert = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.isEnabled = false
        CityRepository().list({ loaded ->
            cities = loaded
            citySpinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities.map { "${it.name}, ${it.state}" })
            btnConfirm.isEnabled = cities.isNotEmpty()
        }, { message -> Toast.makeText(this, message, Toast.LENGTH_LONG).show() })
        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            val mode = if (modeSpinner.selectedItemPosition == 0) "tourism" else "work"
            if (name.isEmpty() || mode.isEmpty()) {
                Toast.makeText(this, "Informe nome e modo da viagem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = AuthRepository(this).getUser()?.id ?: return@setOnClickListener
            val cityId = cities.getOrNull(citySpinner.selectedItemPosition)?.id ?: return@setOnClickListener
            viewModel.createTrip(userId, name, mode, cityId)
            alert.dismiss()
        }

        btnCancel.setOnClickListener { alert.dismiss() }

        alert.show()
    }

    private fun openTripDashboard(tripId: String) {
        val placeId = intent.getStringExtra("add_place_id")?.toLongOrNull()
        val selectedTripId = tripId.toLongOrNull() ?: return
        if (placeId != null) {
            val repository = TripRepository()
            val onError: (String) -> Unit = { message -> Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
            repository.stops(selectedTripId, { stops ->
                val order = (stops.filter { it.day == 1 }.mapNotNull { it.orderIndex }.maxOrNull() ?: 0) + 1
                repository.addStop(com.senac.viaja.data.model.TripLocal(selectedTripId, placeId, 1, order, null), {
                    intent.removeExtra("add_place_id")
                    Toast.makeText(this, "Lugar adicionado ao primeiro dia", Toast.LENGTH_SHORT).show()
                    openTripDashboard(tripId)
                }, onError)
            }, onError)
            return
        }
        val intent = Intent(this@TripListActivity, TripDashboardActivity::class.java)
        intent.putExtra("trip_id", tripId)
        startActivity(intent)
    }
}
