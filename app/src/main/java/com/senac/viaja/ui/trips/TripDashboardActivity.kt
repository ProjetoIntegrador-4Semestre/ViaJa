package com.senac.viaja.ui.trips

import androidx.activity.enableEdgeToEdge
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.TripRepository
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.data.repository.PlaceCallback
import com.senac.viaja.data.model.Place
import com.senac.viaja.ui.places.AiTripChatActivity

class TripDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AuthRepository(this).getUser() == null) {
            startActivity(Intent(this, com.senac.viaja.ui.auth.LoginActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_dashboard)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
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
        val chat = findViewById<MaterialButton>(R.id.btnChatVi)
        chat.isEnabled = false
        title.text = "Carregando viagem…"
        stopName.text = "Carregando roteiro…"
        stopTime.text = ""
        findViewById<TextView>(R.id.tvWeatherTemp).text = "—"
        findViewById<TextView>(R.id.tvWeatherDesc).text = "Clima indisponível"
        val repository = TripRepository()
        val onError: (String) -> Unit = { message ->
            title.text = "Não foi possível carregar a viagem"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
        repository.getById(tripId, { trip ->
            title.text = trip.name
            dates.text = if (trip.startDate == null) "Datas a definir" else "${trip.startDate} — ${trip.endDate ?: "Fim a definir"}"
            mode.text = if (trip.mode == "work") "Trabalho" else "Turismo"
            chat.isEnabled = true
            chat.setOnClickListener {
                startActivity(Intent(this, AiTripChatActivity::class.java)
                    .putExtra("trip_id", trip.id).putExtra("trip_name", trip.name).putExtra("trip_mode", trip.mode))
            }
            repository.stops(tripId, { stops ->
                val stop = stops.firstOrNull()
                if (stop?.localId == null) {
                    stopName.text = "Nenhum lugar no roteiro"
                    return@stops
                }
                stopTime.text = "Dia ${stop.day} • Parada ${stop.orderIndex}"
                PlaceRepository().getPlace(stop.localId.toString(), object : PlaceCallback {
                    override fun onSuccess(places: List<Place>) {}
                    override fun onPlace(place: Place) { stopName.text = place.name }
                    override fun onError(message: String) { stopName.text = message }
                })
            }, { message -> stopName.text = message })
        }, onError)
    }
}
