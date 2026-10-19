package com.senac.viaja.ui.trips

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.senac.viaja.R
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.places.AiTripChatActivity

class TripDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tripId = intent.getStringExtra("trip_id") ?: ""

        val trip = MockDataSource.getTripById(tripId)

        val tvTripDestination = findViewById<android.widget.TextView>(R.id.tvTripDestination)
        val tvTripDates = findViewById<android.widget.TextView>(R.id.tvTripDates)
        val tvTripMode = findViewById<android.widget.TextView>(R.id.tvTripMode)
        val tvNextStopName = findViewById<android.widget.TextView>(R.id.tvNextStopName)
        val tvNextStopTime = findViewById<android.widget.TextView>(R.id.tvNextStopTime)
        val tvWeatherTemp = findViewById<android.widget.TextView>(R.id.tvWeatherTemp)
        val tvWeatherDesc = findViewById<android.widget.TextView>(R.id.tvWeatherDesc)

        // Dados da viagem (vem da API/mock)
        tvTripDestination.text = trip?.name ?: "Viagem"
        tvTripDates.text = if (trip?.startDate != null && trip?.endDate != null) {
            "${trip.startDate} - ${trip.endDate}"
        } else {
            "Datas a definir"
        }
        tvTripMode.text = trip?.mode ?: "Turismo"

        // Próxima parada (exemplo estático da UI, dados viriam da API)
        tvNextStopName.text = "Pão de Açúcar"
        tvNextStopTime.text = "14:00 - Não esquecer o ingresso do teleférico!"

        // Clima (dado dinâmico simulado)
        tvWeatherTemp.text = "28°C"
        tvWeatherDesc.text = "Sensação térmica de 31°C. Ensolarado."

        // Botão flutuante para abrir chat com a IA (Vi)
        val btnChatVi = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChatVi)
        btnChatVi.setOnClickListener {
            val intent = Intent(this@TripDashboardActivity, AiTripChatActivity::class.java)
            intent.putExtra("trip_id", tripId)
            intent.putExtra("trip_name", trip?.name ?: "")
            intent.putExtra("trip_mode", trip?.mode ?: "")
            startActivity(intent)
        }
    }
}
