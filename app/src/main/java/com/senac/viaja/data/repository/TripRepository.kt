package com.senac.viaja.data.repository

import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TripRepository {
    fun list(onSuccess: (List<Trip>) -> Unit, onError: (String) -> Unit) = loadPage(null, 0, mutableListOf(), onSuccess, onError)

    fun listByUser(userId: Long, onSuccess: (List<Trip>) -> Unit, onError: (String) -> Unit) = loadPage(userId, 0, mutableListOf(), onSuccess, onError)

    private fun loadPage(userId: Long?, page: Int, items: MutableList<Trip>, onSuccess: (List<Trip>) -> Unit, onError: (String) -> Unit) {
        val call = if (userId == null) ApiClient.service.getTrips(page) else ApiClient.service.getTripsByUser(userId, page)
        receive(call, { body ->
            items.addAll(body.embedded?.trips.orEmpty())
            if (page + 1 < body.page.totalPages) loadPage(userId, page + 1, items, onSuccess, onError)
            else onSuccess(items)
        }, onError)
    }

    fun stops(id: Long, onSuccess: (List<TripLocal>) -> Unit, onError: (String) -> Unit) = receive(ApiClient.service.getTripLocalsByTrip(id), onSuccess, onError)

    fun updateStopSchedule(tripId: Long, localId: Long, schedule: StopSchedule,
                           onSuccess: (TripLocal) -> Unit, onError: (String) -> Unit) =
        receive(ApiClient.service.updateStopSchedule(tripId, localId, schedule), onSuccess, onError)

    fun notes(tripId: Long, onSuccess: (List<TripNote>) -> Unit, onError: (String) -> Unit) =
        receive(ApiClient.service.getTripNotes(tripId), { onSuccess(it._embedded?.notes.orEmpty()) }, onError)

    fun memos(tripId: Long, localId: Long, onSuccess: (List<TripMemo>) -> Unit, onError: (String) -> Unit) =
        receive(ApiClient.service.getTripMemos(tripId, localId), onSuccess, onError)

    fun addStop(stop: TripLocal, onSuccess: (TripLocal) -> Unit, onError: (String) -> Unit) = receive(ApiClient.service.addTripLocal(stop), onSuccess, onError)

    fun create(request: TripRequest, onSuccess: (TripResponse) -> Unit, onError: (String) -> Unit) = receive(ApiClient.service.createTrip(request), onSuccess, onError)

    fun getById(id: Long, onSuccess: (TripResponse) -> Unit, onError: (String) -> Unit) = receive(ApiClient.service.getTripById(id), onSuccess, onError)

    fun update(id: Long, request: TripRequest, onSuccess: (TripResponse) -> Unit, onError: (String) -> Unit) = receive(ApiClient.service.updateTrip(id, request), onSuccess, onError)

    fun delete(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        ApiClient.service.deleteTrip(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) onSuccess() else onError("Não foi possível excluir a viagem (${response.code()}).")
            }
            override fun onFailure(call: Call<Void>, error: Throwable) { onError("Não foi possível conectar ao servidor.") }
        })
    }

    private fun <T> receive(call: Call<T>, onSuccess: (T) -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body)
                else onError("Não foi possível carregar ou salvar a viagem (${response.code()}).")
            }
            override fun onFailure(call: Call<T>, error: Throwable) { onError("Não foi possível conectar ao servidor.") }
        })
    }
}
