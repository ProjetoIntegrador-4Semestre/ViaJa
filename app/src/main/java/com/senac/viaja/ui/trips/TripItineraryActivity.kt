package com.senac.viaja.ui.trips

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.senac.viaja.R
import com.senac.viaja.data.model.StopSchedule
import com.senac.viaja.data.model.TripLocal
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.data.repository.TripRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.ui.places.AiTripChatActivity
import com.senac.viaja.ui.places.PlaceDetailsActivity
import java.util.Locale

class TripItineraryActivity : AppCompatActivity() {
    private val trips = TripRepository()
    private val places = PlaceRepository()
    private var savingSchedule = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AuthRepository(this).getUser() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val tripId = intent.getLongExtra(TRIP_ID, 0)
        if (tripId <= 0) {
            finish()
            return
        }
        val tab = if (intent.getIntExtra(PAGE_TAB_EXTRA, R.id.nav_trips) == R.id.nav_home) PageTab.HOME else PageTab.TRIPS
        val title = setPageContent(R.layout.activity_trip_itinerary, "Roteiro", tab) { finish() }
        findViewById<View>(R.id.itineraryRetry).setOnClickListener { loadItinerary(tripId, title, tab) }
        loadItinerary(tripId, title, tab)
    }

    private fun loadItinerary(tripId: Long, title: TextView, tab: PageTab) {
        val rows = findViewById<LinearLayout>(R.id.itineraryDays)
        rows.removeAllViews()
        showState("Carregando roteiro…", loading = true)
        val onError: (String) -> Unit = {
            if (!isFinishing && !isDestroyed) showState("Não foi possível carregar o roteiro", retry = true)
        }
        trips.getById(tripId, { trip ->
            if (!isFinishing && !isDestroyed) {
                title.text = trip.name
                findViewById<TextView>(R.id.itineraryTripName).text = trip.name
                findViewById<View>(R.id.pageAi).setOnClickListener {
                    startActivity(Intent(this, AiTripChatActivity::class.java)
                        .putExtra(PAGE_TAB_EXTRA, tab.menuId)
                        .putExtra("trip_name", trip.name)
                        .putExtra("trip_mode", trip.mode))
                }
                trips.stops(tripId, { stops ->
                    if (!isFinishing && !isDestroyed) {
                        showState(if (stops.isEmpty()) "Seu roteiro está vazio" else "")
                        showDays(rows, stops, tab) { loadItinerary(tripId, title, tab) }
                    }
                }, onError)
            }
        }, onError)
    }

    private fun showDays(rows: LinearLayout, stops: List<TripLocal>, tab: PageTab, refresh: () -> Unit) {
        val byDay = stops.sortedWith(itineraryOrder)
            .groupBy { it.day ?: 1 }
        byDay.forEach { (day, entries) ->
            val section = layoutInflater.inflate(R.layout.item_itinerary_day, rows, false) as LinearLayout
            section.findViewById<TextView>(R.id.itineraryDay).text = "Dia $day"
            rows.addView(section)
            entries.forEach { stop -> addStop(section, stop, tab, refresh) }
        }
    }

    private fun addStop(section: LinearLayout, stop: TripLocal, tab: PageTab, refresh: () -> Unit) {
        val row = layoutInflater.inflate(R.layout.item_itinerary_stop, section, false)
        val name = row.findViewById<TextView>(R.id.itineraryPlaceName)
        val note = row.findViewById<TextView>(R.id.itineraryPlaceNote)
        note.text = stop.note
        note.isVisible = !stop.note.isNullOrBlank()
        val time = stop.displayTime ?: "Horário a definir"
        row.findViewById<TextView>(R.id.itineraryPlaceTime).text =
            if (stop.completed) "$time · Concluída" else time
        section.addView(row)

        val placeId = stop.localId?.toString()
        if (placeId == null) {
            name.text = "Lugar indisponível"
            return
        }

        row.setOnClickListener {
            startActivity(Intent(this, PlaceDetailsActivity::class.java)
                .putExtra("place_id", placeId)
                .putExtra(PAGE_TAB_EXTRA, tab.menuId))
        }
        places.getPlace(placeId, { name.text = it.name }, { name.text = "Lugar indisponível" })
        val action = row.findViewById<View>(R.id.itinerarySchedule)
        action.isEnabled = stop.tripId != null
        action.setOnClickListener {
            if (!savingSchedule) editSchedule(stop, refresh)
        }
    }

    private fun editSchedule(stop: TripLocal, refresh: () -> Unit) {
        val tripId = stop.tripId ?: return
        val localId = stop.localId ?: return
        val save: (StopSchedule) -> Unit = { schedule ->
            savingSchedule = true
            trips.updateStopSchedule(tripId, localId, schedule, {
                savingSchedule = false
                if (!isFinishing && !isDestroyed) refresh()
            }, {
                savingSchedule = false
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, "Não foi possível atualizar a parada. Tente novamente.", Toast.LENGTH_LONG).show()
                }
            })
        }

        val actions = mutableListOf("Definir horário")
        if (stop.displayTime != null) actions.add("Remover horário")
        actions.add(if (stop.completed) "Reabrir parada" else "Marcar como concluída")
        AlertDialog.Builder(this)
            .setTitle("Horário e conclusão")
            .setItems(actions.toTypedArray()) { _, index ->
                when (actions[index]) {
                    "Definir horário" -> {
                        val parts = stop.displayTime?.split(":")
                        TimePickerDialog(this, { _, hour, minute ->
                            val time = String.format(Locale.US, "%02d:%02d", hour, minute)
                            save(StopSchedule(time, stop.completed))
                        }, parts?.get(0)?.toInt() ?: 9, parts?.get(1)?.toInt() ?: 0, true).show()
                    }
                    "Remover horário" -> save(StopSchedule(null, stop.completed))
                    else -> save(StopSchedule(stop.displayTime, !stop.completed))
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showState(message: String, loading: Boolean = false, retry: Boolean = false) {
        val state = findViewById<TextView>(R.id.itineraryStatus)
        state.text = message
        state.isVisible = message.isNotBlank()
        findViewById<View>(R.id.itineraryLoading).isVisible = loading
        findViewById<View>(R.id.itineraryRetry).isVisible = retry
    }

    companion object {
        const val TRIP_ID = "itinerary_trip_id"
    }
}
