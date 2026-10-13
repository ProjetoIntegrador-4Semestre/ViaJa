package com.senac.viaja.data.repository

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.mock.MockDataSource

class PlaceRepository {

    private val handler = Handler(Looper.getMainLooper())

    fun search(query: String, callback: PlaceCallback) {
        handler.postDelayed({
            val places = MockDataSource.searchPlaces(query)
            callback.onSuccess(places)
        }, 600)
    }

    fun getPlace(id: String, callback: PlaceCallback) {
        handler.postDelayed({
            MockDataSource.getPlaceById(id)?.let { callback.onPlace(it) }
                ?: callback.onError("Lugar não encontrado")
        }, 600)
    }

    fun searchByCity(city: String, callback: PlaceCallback) {
        handler.postDelayed({
            val places = MockDataSource.getPlacesByCity(city)
            callback.onSuccess(places)
        }, 600)
    }
}

interface PlaceCallback {
    fun onSuccess(places: List<Place>)
    fun onPlace(place: Place)
    fun onError(message: String)
}
