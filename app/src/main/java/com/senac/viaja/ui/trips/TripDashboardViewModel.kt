package com.senac.viaja.ui.trips

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.senac.viaja.data.model.TripRequest
import com.senac.viaja.data.model.TripResponse
import com.senac.viaja.data.repository.CityRepository
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.data.repository.TripRepository

class TripDashboardViewModel : ViewModel() {
    val changed = MutableLiveData(0)
    var trip: TripResponse? = null
    var tripId = 0L
    var ownerId = 0L
    var loading = false
    var mutating = false
    var error: String? = null
    var stopError: String? = null
    var city = ""
    var stopTitle = "Roteiro"
    var stopName = ""
    var stopTime = ""
    var progress = ""
    var deleted = false
    var expenseSummary = ""
    private var version = 0
    private val trips = TripRepository()

    private fun notifyChanged() { changed.value = (changed.value ?: 0) + 1 }

    fun refresh() {
        if (loading || mutating || tripId <= 0) return
        val request = ++version
        loading = true
        error = null
        stopError = null
        notifyChanged()
        trips.getById(tripId, { loaded ->
            if (request != version) return@getById
            trip = loaded
            loading = false
            city = "Consultando destino…"
            notifyChanged()
            loaded.cityId?.let { id ->
                CityRepository().getById(id, { destination ->
                    if (request == version) {
                        city = listOfNotNull(destination.name, destination.state).joinToString(", ")
                        notifyChanged()
                    }
                }, {
                    if (request == version) { city = "Destino indisponível"; notifyChanged() }
                })
            } ?: run { city = "Destino não informado"; notifyChanged() }
            loadStops(request, loaded.startDate)
            expenseSummary = "Carregando gastos…"
            notifyChanged()
            com.senac.viaja.data.repository.ExpenseRepository().report(tripId, { report, available ->
                if (request == version) {
                    expenseSummary = if (available && report != null)
                        "Total registrado · ${ExpenseRules.currency(report.totalCents)}\n${ExpenseRules.balanceLabel(report.remainingCents)}"
                    else "O registro de gastos ainda não está disponível."
                    notifyChanged()
                }
            }, {
                if (request == version) { expenseSummary = "Não foi possível atualizar os gastos. Abra a página para tentar novamente."; notifyChanged() }
            })
        }, {
            if (request == version) { loading = false; error = it; notifyChanged() }
        })
    }

    private fun loadStops(request: Int, startDate: String?) {
        stopTitle = "Roteiro"
        stopName = "Carregando paradas…"
        stopTime = ""
        progress = ""
        notifyChanged()
        trips.stops(tripId, { stops ->
            if (request != version) return@stops
            val preview = previewStop(stops, startDate)
            val stop = preview.stop
            stopTitle = preview.title
            progress = if (stops.isEmpty()) "Adicione lugares para começar seu roteiro."
                else "${stops.count { it.completed }} de ${stops.size} paradas concluídas"
            stopName = ""
            if (stop != null) {
                stopTime = "Dia ${stop.day ?: 1} · ${stop.displayTime ?: "Horário a definir"}"
                val placeId = stop.localId
                if (placeId == null) stopName = "Lugar indisponível"
                else PlaceRepository().getPlace(placeId.toString(), {
                    if (request == version) { stopName = it.name.orEmpty(); notifyChanged() }
                }, {
                    if (request == version) { stopName = "Lugar indisponível"; notifyChanged() }
                })
            }
            notifyChanged()
        }, {
            if (request == version) { stopName = "Não foi possível carregar as paradas."; stopError = it; notifyChanged() }
        })
    }

    fun delete() {
        if (mutating || loading || trip == null) return
        mutating = true
        error = null
        ++version
        notifyChanged()
        trips.delete(tripId, { mutating = false; deleted = true; notifyChanged() }, {
            mutating = false
            error = "Não foi possível excluir a viagem. Seus dados foram mantidos. Tente novamente."
            notifyChanged()
        })
    }

    fun updateStatus(status: String) {
        val current = trip ?: return
        if (mutating || loading || status == current.status) return
        val cityId = current.cityId ?: run { error = "Recarregue a viagem antes de alterar seu estado."; notifyChanged(); return }
        mutating = true
        error = null
        ++version
        notifyChanged()
        trips.update(tripId, TripRequest(ownerId, current.name, current.mode, current.startDate, current.endDate,
            current.totalBudgetCents, cityId, status), {
            trip = it
            mutating = false
            notifyChanged()
            refresh()
        }, { mutating = false; error = it; notifyChanged() })
    }
}
