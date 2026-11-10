package com.senac.viaja.data.repository

import com.senac.viaja.data.model.CatalogPlace
import com.senac.viaja.data.model.CatalogPlacePage
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaceRepository {

    fun nearbyCandidates(onSuccess: (List<CatalogPlace>) -> Unit, onError: () -> Unit) {
        ApiClient.service.getPlaces(size = 50).enqueue(object : Callback<CatalogPlacePage> {
            override fun onResponse(call: Call<CatalogPlacePage>, response: Response<CatalogPlacePage>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body.embedded?.localList.orEmpty())
                else onError()
            }

            override fun onFailure(call: Call<CatalogPlacePage>, error: Throwable) = onError()
        })
    }

    fun search(query: String, callback: PlaceCallback) {
        ApiClient.service.searchPlaces(query.trim()).enqueue(object : Callback<List<Place>> {
            override fun onResponse(call: Call<List<Place>>, response: Response<List<Place>>) {
                val places = response.body()
                if (response.isSuccessful && places != null) callback.onSuccess(places)
                else callback.onError("Não foi possível carregar os lugares (${response.code()}).")
            }

            override fun onFailure(call: Call<List<Place>>, error: Throwable) {
                callback.onError("Não foi possível conectar ao servidor. Tente novamente.")
            }
        })
    }

    fun getPlace(id: String, callback: PlaceCallback) = getPlace(id, callback::onPlace, callback::onError)

    fun getPlace(id: String, onSuccess: (Place) -> Unit, onError: (String) -> Unit) {
        if (id.toLongOrNull() == null) {
            onError("Lugar inválido.")
            return
        }
        ApiClient.service.getPlace(id).enqueue(object : Callback<Place> {
            override fun onResponse(call: Call<Place>, response: Response<Place>) {
                val place = response.body()
                if (response.isSuccessful && place != null) onSuccess(place)
                else onError(if (response.code() == 404) "Lugar não encontrado." else "Não foi possível carregar o lugar (${response.code()}).")
            }

            override fun onFailure(call: Call<Place>, error: Throwable) {
                onError("Não foi possível conectar ao servidor. Tente novamente.")
            }
        })
    }

    fun searchByCity(city: String, callback: PlaceCallback) = search(city, callback)
}

interface PlaceCallback {
    fun onSuccess(places: List<Place>)
    fun onPlace(place: Place)
    fun onError(message: String)
}
