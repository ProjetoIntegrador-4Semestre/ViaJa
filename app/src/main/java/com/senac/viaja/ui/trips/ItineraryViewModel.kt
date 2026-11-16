package com.senac.viaja.ui.trips

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.senac.viaja.data.model.*
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.data.repository.TripRepository

class ItineraryViewModel : ViewModel() {
    val changed = MutableLiveData(0)
    var tripId = 0L
    var ownerId = 0L
    var trip: TripResponse? = null
    var original = emptyList<TripLocal>()
    var draft = emptyList<TripLocal>()
    var selectedDay = 1
    var loading = false
    var saving = false
    var fullEditing = false
    var error: String? = null
    var notice: String? = null
    var ready = false
    val names = mutableMapOf<Long, String>()
    var catalog = emptyList<ItineraryPlace>()
    var showCatalog = false
    var incomingPlaceId: Long? = null
    var catalogLoading = false
    private var version = 0
    private val repository = TripRepository()
    val dirty get() = draft != original
    val duration get() = ItineraryRules.duration(trip?.startDate, trip?.endDate)
    val dayLimit get() = maxOf(duration ?: selectedDay, draft.maxOfOrNull { it.day ?: 1 } ?: 1)

    private fun notifyChanged() { changed.value = (changed.value ?: 0) + 1 }

    fun load() {
        if (loading || saving) return
        val request = ++version
        loading = true
        error = null
        notice = null
        notifyChanged()
        repository.getById(tripId, { loaded ->
            if (request != version) return@getById
            repository.editableItinerary(tripId, { stops, supported ->
                if (request != version) return@editableItinerary
                trip = loaded
                original = stops.sortedWith(itineraryOrder)
                draft = original
                fullEditing = supported
                selectedDay = selectedDay.coerceIn(1, maxOf(duration ?: selectedDay, stops.maxOfOrNull { it.day ?: 1 } ?: 1))
                ready = true
                loading = false
                notifyChanged()
                stops.mapNotNull { it.localId }.distinct().forEach { id ->
                    PlaceRepository().getPlace(id.toString(), { place ->
                        if (request == version) { names[id] = place.name ?: "Lugar"; notifyChanged() }
                    }, { if (request == version) { names[id] = "Lugar indisponível"; notifyChanged() } })
                }
            }, { fail(it) })
        }, { fail(it) })
    }

    private fun fail(message: String) { loading = false; saving = false; catalogLoading = false; error = message; notifyChanged() }

    fun selectDay(day: Int) {
        if (day < 1 || (duration != null && day > maxOf(duration!!, draft.maxOfOrNull { it.day ?: 1 } ?: 1))) return
        selectedDay = day
        notifyChanged()
    }

    fun edit(stop: TripLocal, day: Int = stop.day ?: 1, position: Int? = null, note: String? = stop.note,
             time: String? = stop.scheduledTime, completed: Boolean = stop.completed) {
        if (saving || loading) return
        if (!fullEditing) {
            if (dirty) { fail("Recarregue para editar horários neste momento, ou preserve o rascunho para salvar mais tarde."); return }
            if (day != stop.day || position != null || note != stop.note) return
            val localId = stop.localId ?: return
            saving = true
            error = null
            notifyChanged()
            repository.updateStopSchedule(tripId, localId, StopSchedule(time, completed), {
                saving = false
                load()
            }, ::fail)
            return
        }
        if (duration != null && day > duration!!) { fail("Escolha um dia dentro das datas da viagem."); return }
        var updated = draft.map { if (it.localId == stop.localId) it.copy(note = note, scheduledTime = time, completed = completed) else it }
        if (day != stop.day || position != null) updated = ItineraryRules.move(updated, stop.localId ?: return, day,
            position ?: updated.count { it.day == day && it.localId != stop.localId })
        draft = updated
        selectedDay = day
        error = null
        notice = null
        notifyChanged()
    }

    fun remove(stop: TripLocal) {
        if (!fullEditing || saving || loading) return
        draft = ItineraryRules.normalize(draft.filterNot { it.localId == stop.localId })
        error = null
        notifyChanged()
    }

    fun loadCatalog() {
        if (catalogLoading) return
        catalogLoading = true
        error = null
        notifyChanged()
        repository.itineraryPlaces(tripId, {
            catalog = it
            catalogLoading = false
            showCatalog = true
            notifyChanged()
        }, ::fail)
    }

    fun add(place: ItineraryPlace, day: Int, time: String?) {
        if (!fullEditing || saving || loading) return
        val id = place.id
        if (draft.any { it.localId == id }) { fail("Este lugar já está no roteiro. Você pode movê-lo para outro dia."); return }
        val updated = draft + TripLocal(tripId, id, day, draft.count { it.day == day } + 1, null, time)
        runCatching { ItineraryRules.validate(updated, duration) }.onFailure { fail(it.message ?: "Revise a parada."); return }
        names[id] = place.name
        draft = updated
        selectedDay = day
        notifyChanged()
    }

    fun save() {
        if (!fullEditing || loading || saving || !dirty) return
        val normalized = ItineraryRules.normalize(draft.sortedWith(itineraryOrder))
        val request = runCatching {
            ItineraryRules.validate(normalized, duration)
            ItinerarySaveRequest(ItineraryRules.entries(original), ItineraryRules.entries(normalized))
        }.getOrElse { fail(it.message ?: "Revise seu roteiro."); return }
        saving = true
        error = null
        notifyChanged()
        repository.saveItinerary(tripId, request, { saved ->
            saving = false
            original = saved.sortedWith(itineraryOrder)
            draft = original
            notice = "Roteiro salvo."
            notifyChanged()
        }, ::fail)
    }
}
