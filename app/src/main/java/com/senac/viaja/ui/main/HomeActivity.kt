package com.senac.viaja.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.senac.viaja.R
import com.senac.viaja.data.model.Trip
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.CityRepository
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.data.repository.SavedRepository
import com.senac.viaja.data.repository.TripRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.places.AiTripChatActivity
import com.senac.viaja.ui.places.PlaceDetailsActivity
import com.senac.viaja.ui.trips.TripItineraryActivity
import com.senac.viaja.ui.trips.displayTime
import com.senac.viaja.ui.trips.previewStop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HomeActivity : AppCompatActivity() {
    private val auth by lazy { AuthRepository(this) }
    private val trips = TripRepository()
    private val places = PlaceRepository()
    private val cities = CityRepository()
    private val saved = SavedRepository()
    private val nearby by lazy { NearbyPlaces(this) }
    private var nearbyVersion = 0
    private var requestingLocationPermission = false
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) loadNearby()
        else showNearbyStatus("Permita a localização para ver lugares por perto ou continue em Explorar.")
        window.decorView.post { requestingLocationPermission = false }
    }
    private var requestVersion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_home, "Início", PageTab.HOME, topLevel = true)

        findViewById<View>(R.id.homeRetry).setOnClickListener { loadHome() }
        findViewById<View>(R.id.homeExplore).setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        findViewById<View>(R.id.homeOpenSaved).setOnClickListener {
            startActivity(Intent(this, SavedItemsActivity::class.java))
        }
        findViewById<View>(R.id.homeNearbyExplore).setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        findViewById<View>(R.id.homeLocate).setOnClickListener {
            if (nearby.hasPermission) {
                loadNearby()
            } else {
                requestingLocationPermission = true
                locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        findViewById<View>(R.id.homeLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (!requestingLocationPermission) loadHome()
    }

    override fun onStop() {
        nearbyVersion++
        nearby.cancel()
        val locate = findViewById<View>(R.id.homeLocate)
        if (!locate.isEnabled) showNearbyStatus("Consulte os lugares próximos quando quiser.")
        super.onStop()
    }

    private fun loadHome() {
        val version = ++requestVersion
        val userId = auth.getUser()?.id
        findViewById<View>(R.id.homeTrip).isVisible = false
        findViewById<View>(R.id.homeState).isVisible = true
        findViewById<View>(R.id.homeRetryStop).isVisible = false
        bindVi(null)
        loadSavedPreview(userId, version)
        if (userId == null) {
            showEmpty(visitor = true)
            return
        }

        showState("Carregando suas viagens…", "Estamos preparando seu painel.", loading = true)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        trips.listByUser(userId, { results ->
            if (isCurrent(version, userId)) {
                val trip = selectHomeTrip(results, today)
                if (trip == null) showEmpty(visitor = false) else showTrip(trip, today, version, userId)
            }
        }, {
            if (isLatestRequest(version)) {
                if (auth.getUser()?.id == userId) {
                    showState("Não foi possível carregar suas viagens", "Confira a conexão e tente novamente.", retry = true)
                } else {
                    loadHome()
                }
            }
        })
    }

    private fun showEmpty(visitor: Boolean) {
        val message = if (visitor) "Entre para ver suas viagens. Para começar, explore destinos ou toque no ícone da Vi no cabeçalho."
        else "Explore destinos ou toque no ícone da Vi no cabeçalho para começar a planejar."
        showState(if (visitor) "Sua viagem começa aqui" else "Nenhuma viagem disponível", message)
        findViewById<View>(R.id.homeExplore).isVisible = true
        findViewById<View>(R.id.homeLogin).isVisible = visitor
    }

    private fun showState(title: String, detail: String, loading: Boolean = false, retry: Boolean = false) {
        findViewById<TextView>(R.id.homeStateTitle).text = title
        findViewById<TextView>(R.id.homeStateDetail).text = detail
        findViewById<View>(R.id.homeLoading).isVisible = loading
        findViewById<View>(R.id.homeRetry).isVisible = retry
        findViewById<View>(R.id.homeExplore).isVisible = false
        findViewById<View>(R.id.homeLogin).isVisible = false
    }

    private fun showTrip(trip: Trip, today: String, version: Int, userId: Long) {
        val tripId = trip.id?.toLongOrNull() ?: return
        findViewById<View>(R.id.homeState).isVisible = false
        findViewById<View>(R.id.homeTrip).isVisible = true
        findViewById<TextView>(R.id.homeTripStatus).text = when {
            trip.startDate?.let { it > today } == true -> "Próxima viagem"
            trip.status == "active" -> "Viagem em andamento"
            trip.startDate != null -> "Viagem de hoje"
            else -> "Viagem planejada"
        }
        findViewById<TextView>(R.id.homeTripName).text = trip.name ?: "Sua viagem"
        val dates = listOfNotNull(trip.startDate, trip.endDate).distinct()
        findViewById<TextView>(R.id.homeTripDates).text = if (dates.isEmpty()) "Datas a definir"
        else dates.joinToString(" – ") { it.split("-").reversed().joinToString("/") }

        val cityView = findViewById<TextView>(R.id.homeTripCity)
        cityView.isVisible = trip.cityId != null
        cityView.text = "Carregando destino…"
        trip.cityId?.let { cityId ->
            cities.getById(cityId, { city ->
                if (isCurrent(version, userId)) cityView.text = city.name ?: "Destino indisponível"
            }, {
                if (isCurrent(version, userId)) cityView.text = "Destino indisponível"
            })
        }

        findViewById<View>(R.id.homeOpenItinerary).setOnClickListener {
            startActivity(Intent(this, TripItineraryActivity::class.java)
                .putExtra(TripItineraryActivity.TRIP_ID, tripId)
                .putExtra(PAGE_TAB_EXTRA, R.id.nav_home))
        }
        findViewById<View>(R.id.homeRetryStop).setOnClickListener { loadStop(tripId, trip.startDate, version, userId) }
        loadStop(tripId, trip.startDate, version, userId)
        val note = findViewById<TextView>(R.id.homeTripNote)
        note.text = "Carregando nota da viagem…"
        trips.notes(tripId, { notes ->
            if (isCurrent(version, userId)) {
                note.text = notes.firstNotNullOfOrNull { it.note?.takeIf(String::isNotBlank) }
                    ?: "Nenhuma nota nesta viagem."
            }
        }, {
            if (isCurrent(version, userId)) note.text = "Nota da viagem indisponível."
        })
        bindVi(trip)
    }

    private fun loadStop(tripId: Long, startDate: String?, version: Int, userId: Long) {
        val title = findViewById<TextView>(R.id.homeStopTitle)
        val name = findViewById<TextView>(R.id.homeStopName)
        val detail = findViewById<TextView>(R.id.homeStopDetail)
        val memo = findViewById<TextView>(R.id.homeStopMemo)
        val retry = findViewById<View>(R.id.homeRetryStop)
        title.text = "Roteiro"
        name.text = "Carregando roteiro…"
        detail.text = ""
        memo.isVisible = false
        retry.isVisible = false

        val showError: (String) -> Unit = {
            if (isCurrent(version, userId)) {
                name.text = "Não foi possível carregar a parada"
                retry.isVisible = true
            }
        }
        trips.stops(tripId, { stops ->
            if (isCurrent(version, userId)) {
                val preview = previewStop(stops, startDate)
                val stop = preview.stop
                val placeId = stop?.localId
                title.text = if (stop == null) "Roteiro" else preview.title
                when {
                    stop == null -> name.text = preview.title
                    placeId == null -> name.text = "Lugar indisponível"
                    else -> {
                        detail.text = "Dia ${stop.day ?: 1} · ${stop.displayTime ?: "Horário a definir"}"
                        places.getPlace(placeId.toString(), { place ->
                            if (isCurrent(version, userId)) name.text = place.name
                        }, showError)
                        trips.memos(tripId, placeId, { memos ->
                            if (isCurrent(version, userId)) {
                                val latest = memos.filter { !it.note.isNullOrBlank() }.maxByOrNull { it.id ?: 0 }
                                memo.text = latest?.note?.let { "Memo: $it" }
                                memo.isVisible = latest != null
                            }
                        }, {
                            if (isCurrent(version, userId)) {
                                memo.text = "Memo da parada indisponível."
                                memo.isVisible = true
                            }
                        })
                    }
                }
            }
        }, showError)
    }

    private fun loadSavedPreview(userId: Long?, version: Int) {
        val card = findViewById<View>(R.id.homeSavedCard)
        val rows = findViewById<LinearLayout>(R.id.homeSavedRows)
        card.isVisible = userId != null
        rows.removeAllViews()
        if (userId == null) return

        val status = findViewById<TextView>(R.id.homeSavedStatus)
        status.text = "Carregando seus salvos…"
        status.isVisible = true
        saved.preview(userId, { items ->
            if (isCurrent(version, userId)) {
                status.isVisible = items.isEmpty()
                status.text = "Você ainda não salvou lugares."
                items.forEach { item ->
                    val name = item.place?.name ?: "Lugar salvo · detalhes indisponíveis"
                    addPlaceRow(rows, name, item.localId)
                }
            }
        }, {
            if (isCurrent(version, userId)) status.text = "Não foi possível carregar a prévia dos salvos."
        })
    }

    private fun loadNearby() {
        val version = ++nearbyVersion
        val rows = findViewById<LinearLayout>(R.id.homeNearbyRows)
        rows.removeAllViews()
        findViewById<View>(R.id.homeLocate).isEnabled = false
        val status = findViewById<TextView>(R.id.homeNearbyStatus)
        status.text = "Buscando lugares próximos…"
        status.isVisible = true

        nearby.locate { location ->
            if (version == nearbyVersion) {
                if (location == null) {
                    showNearbyStatus("Localização indisponível. Você pode tentar novamente ou abrir Explorar.")
                } else {
                    places.nearbyCandidates({ candidates ->
                        if (version == nearbyVersion) {
                            val results = closestPlaces(location, candidates)
                            if (results.isEmpty()) {
                                showNearbyStatus("Nenhum lugar do catálogo encontrado em até 20 km.")
                            } else {
                                status.text = "Distâncias aproximadas em linha reta."
                                findViewById<View>(R.id.homeLocate).isEnabled = true
                                results.forEach { (place, meters) ->
                                    val distance = if (meters < 1_000) "menos de 1 km"
                                    else "${(meters / 1_000).roundToInt()} km"
                                    addPlaceRow(rows, "${place.name} · $distance", place.id)
                                }
                            }
                        }
                    }, {
                        if (version == nearbyVersion) showNearbyStatus("Não foi possível carregar lugares próximos.")
                    })
                }
            }
        }
    }

    private fun showNearbyStatus(message: String) {
        findViewById<TextView>(R.id.homeNearbyStatus).text = message
        findViewById<View>(R.id.homeLocate).isEnabled = true
    }

    private fun addPlaceRow(rows: LinearLayout, name: String, placeId: String) {
        val button = layoutInflater.inflate(R.layout.item_home_place, rows, false) as TextView
        button.text = name
        button.setOnClickListener {
            startActivity(Intent(this, PlaceDetailsActivity::class.java)
                .putExtra("place_id", placeId)
                .putExtra(PAGE_TAB_EXTRA, R.id.nav_home))
        }
        rows.addView(button)
    }

    private fun bindVi(trip: Trip?) {
        findViewById<View>(R.id.pageAi).setOnClickListener {
            val chat = Intent(this, AiTripChatActivity::class.java).putExtra(PAGE_TAB_EXTRA, R.id.nav_home)
            trip?.let { chat.putExtra("trip_name", it.name).putExtra("trip_mode", it.mode) }
            startActivity(chat)
        }
    }

    private fun isLatestRequest(version: Int): Boolean =
        !isFinishing && !isDestroyed && version == requestVersion

    private fun isCurrent(version: Int, userId: Long): Boolean =
        isLatestRequest(version) && auth.getUser()?.id == userId
}
