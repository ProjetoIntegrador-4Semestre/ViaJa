package com.senac.viaja.ui.trips

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.TripRepository
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.ui.places.AiTripChatActivity

class TripDashboardActivity : AppCompatActivity() {
    private val trips = TripRepository()
    private val places = PlaceRepository()
    private var refreshStop: (() -> Unit)? = null
    private val itineraryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshStop?.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AuthRepository(this).getUser() == null) {
            startActivity(Intent(this, com.senac.viaja.ui.auth.LoginActivity::class.java))
            finish()
            return
        }
        val headerTitle = setPageContent(R.layout.activity_trip_dashboard, "Viagem", PageTab.TRIPS) { finish() }
        val tripId = intent.getStringExtra("trip_id")?.toLongOrNull()
        if (tripId == null) {
            finish()
            return
        }
        val title = findViewById<TextView>(R.id.tvTripDestination)
        val dates = findViewById<TextView>(R.id.tvTripDates)
        val mode = findViewById<TextView>(R.id.tvTripMode)
        val stopName = findViewById<TextView>(R.id.tvNextStopName)
        val stopTime = findViewById<TextView>(R.id.tvNextStopTime)
        val itinerary = findViewById<MaterialButton>(R.id.btnOpenTripItinerary)
        itinerary.isEnabled = false
        title.text = "Carregando viagem…"
        stopName.text = "Carregando roteiro…"
        stopTime.text = ""
        val onError: (String) -> Unit = { message ->
            title.text = "Não foi possível carregar a viagem"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
        trips.getById(tripId, { trip ->
            headerTitle.text = trip.name
            title.text = trip.name
            dates.text = if (trip.startDate == null) "Datas a definir" else "${trip.startDate} — ${trip.endDate ?: "Fim a definir"}"
            mode.text = if (trip.mode == "work") "Trabalho" else "Turismo"
            itinerary.isEnabled = true
            val openChat = {
                startActivity(Intent(this, AiTripChatActivity::class.java)
                    .putExtra(PAGE_TAB_EXTRA, R.id.nav_trips)
                    .putExtra("trip_name", trip.name)
                    .putExtra("trip_mode", trip.mode))
            }
            itinerary.setOnClickListener {
                itineraryResult.launch(Intent(this, TripItineraryActivity::class.java)
                    .putExtra(TripItineraryActivity.TRIP_ID, tripId))
            }
            findViewById<android.view.View>(R.id.pageAi).setOnClickListener { openChat() }
            val refresh = { loadStop(tripId, trip.startDate) }
            refreshStop = refresh
            refresh()
        }, onError)
    }

    private fun loadStop(tripId: Long, startDate: String?) {
        val name = findViewById<TextView>(R.id.tvNextStopName)
        val time = findViewById<TextView>(R.id.tvNextStopTime)
        name.text = "Carregando roteiro…"
        time.text = ""
        trips.stops(tripId, { stops ->
            val preview = previewStop(stops, startDate)
            val stop = preview.stop
            findViewById<TextView>(R.id.tvNextStop).text = if (stop == null) "Roteiro" else preview.title
            val placeId = stop?.localId
            if (stop == null || placeId == null) {
                name.text = if (stop == null) preview.title else "Lugar indisponível"
            } else {
                time.text = "Dia ${stop.day ?: 1} · ${stop.displayTime ?: "Horário a definir"}"
                places.getPlace(placeId.toString(), { name.text = it.name }, { name.text = it })
            }
        }, { name.text = it })
    }
}
