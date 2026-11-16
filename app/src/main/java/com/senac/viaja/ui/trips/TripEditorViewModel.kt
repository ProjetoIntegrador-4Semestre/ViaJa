package com.senac.viaja.ui.trips

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.senac.viaja.data.model.City
import com.senac.viaja.data.model.TripRequest
import com.senac.viaja.data.model.TripResponse
import com.senac.viaja.data.repository.CityRepository
import com.senac.viaja.data.repository.TripRepository

class TripEditorViewModel : ViewModel() {
    val changed = MutableLiveData(0)
    var tripId: Long? = null
    var ownerId: Long = 0
    var name = ""
    var start = ""
    var end = ""
    var budget = ""
    var mode = "tourism"
    var cityId: Long? = null
    var cities = emptyList<City>()
    var original: TripResponse? = null
    var ready = false
    var busy = false
    var error: String? = null
    var saved: TripResponse? = null
    var hasStops = false
    private var initialized = false
    private val trips = TripRepository()

    fun initialize(id: Long?, userId: Long, defaultMode: String?) {
        if (initialized) return
        initialized = true
        tripId = id
        ownerId = userId
        mode = defaultMode?.takeIf { it == "work" } ?: "tourism"
        load()
    }

    private fun notifyChanged() { changed.value = (changed.value ?: 0) + 1 }
    private fun fail(message: String) { busy = false; error = message; notifyChanged() }

    fun load() {
        if (busy) return
        busy = true
        error = null
        notifyChanged()
        CityRepository().list({ loaded ->
            cities = loaded
            if (loaded.isEmpty()) { fail("Nenhum destino disponível. Tente novamente mais tarde."); return@list }
            val id = tripId
            if (id == null) {
                cityId = cities.first().id
                ready = true
                busy = false
                notifyChanged()
            } else {
                trips.getById(id, { trip ->
                    trips.stops(id, { stops ->
                        original = trip
                        name = trip.name
                        start = TripFormRules.displayDate(trip.startDate)
                        end = TripFormRules.displayDate(trip.endDate)
                        budget = TripFormRules.budgetInput(trip.totalBudgetCents)
                        mode = trip.mode
                        cityId = trip.cityId
                        hasStops = stops.isNotEmpty()
                        ready = true
                        busy = false
                        if (cities.none { it.id == cityId }) error = "O destino atual não está disponível. Não é possível salvar esta viagem."
                        notifyChanged()
                    }, ::fail)
                }, ::fail)
            }
        }, ::fail)
    }

    fun save() {
        if (!ready || busy) return
        val selectedCity = cityId
        if (selectedCity == null || cities.none { it.id == selectedCity }) { fail("Escolha um destino válido."); return }
        val initial = runCatching { TripFormRules.validate(name, start, end, budget) }
        if (initial.isFailure) { fail(initial.exceptionOrNull()?.message ?: "Revise os dados."); return }
        busy = true
        error = null
        notifyChanged()
        val id = tripId
        fun send(lastDay: Int, stopsExist: Boolean) {
            val values = runCatching {
                require(!stopsExist || selectedCity == original?.cityId) { "Remova as paradas antes de trocar o destino." }
                TripFormRules.validate(name, start, end, budget, lastDay)
            }.getOrElse { fail(it.message ?: "Revise os dados."); return }
            val request = TripRequest(ownerId, values.name, mode, values.startDate, values.endDate, values.budgetCents, selectedCity, original?.status)
            val success: (TripResponse) -> Unit = { saved = it; busy = false; notifyChanged() }
            if (id == null) trips.create(request, success, ::fail) else trips.update(id, request, success, ::fail)
        }
        if (id == null) send(0, false)
        else trips.stops(id, { stops -> send(stops.maxOfOrNull { it.day ?: 1 } ?: 0, stops.isNotEmpty()) }, ::fail)
    }
}
