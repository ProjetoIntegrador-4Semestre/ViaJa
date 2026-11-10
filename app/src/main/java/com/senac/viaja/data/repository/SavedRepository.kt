package com.senac.viaja.data.repository

import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavedRepository {
    fun list(userId: Long, onSuccess: (List<SavedItem>) -> Unit, onError: (String) -> Unit) {
        loadPage(userId, 0, mutableListOf(), onSuccess, onError)
    }

    fun preview(userId: Long, onSuccess: (List<SavedItem>) -> Unit, onError: (String) -> Unit) {
        fetchPage(userId, 0, size = 3, sort = "id,desc", onSuccess = { page ->
            loadPlaces(page.embedded?.savedLocals.orEmpty().take(3).toMutableList(), 0, onSuccess)
        }, onError = onError)
    }

    private fun loadPage(
        userId: Long,
        page: Int,
        items: MutableList<SavedItem>,
        onSuccess: (List<SavedItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        fetchPage(userId, page, onSuccess = { body ->
            items.addAll(body.embedded?.savedLocals.orEmpty())
            if (page + 1 < body.page.totalPages) loadPage(userId, page + 1, items, onSuccess, onError)
            else loadPlaces(items, 0, onSuccess)
        }, onError = onError)
    }

    private fun fetchPage(
        userId: Long,
        page: Int,
        size: Int? = null,
        sort: String? = null,
        onSuccess: (SavedPage) -> Unit,
        onError: (String) -> Unit
    ) {
        ApiClient.service.getSavedLocals(userId, page, size, sort).enqueue(object : Callback<SavedPage> {
            override fun onResponse(call: Call<SavedPage>, response: Response<SavedPage>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body)
                else onError("Não foi possível carregar os salvos (${response.code()}).")
            }

            override fun onFailure(call: Call<SavedPage>, error: Throwable) {
                onError("Não foi possível conectar ao servidor.")
            }
        })
    }

    private fun loadPlaces(items: MutableList<SavedItem>, index: Int, onSuccess: (List<SavedItem>) -> Unit) {
        if (index == items.size) {
            onSuccess(items)
            return
        }
        ApiClient.service.getPlace(items[index].localId).enqueue(object : Callback<Place> {
            override fun onResponse(call: Call<Place>, response: Response<Place>) {
                val place = response.body()
                if (response.isSuccessful && place != null) items[index] = items[index].copy(place = place)
                loadPlaces(items, index + 1, onSuccess)
            }

            override fun onFailure(call: Call<Place>, error: Throwable) {
                loadPlaces(items, index + 1, onSuccess)
            }
        })
    }

    fun isSaved(userId: Long, localId: Long, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        ApiClient.service.getSavedLocal(userId, localId).enqueue(object : Callback<SavedItem> {
            override fun onResponse(call: Call<SavedItem>, response: Response<SavedItem>) {
                if (response.isSuccessful) onSuccess(true)
                else if (response.code() == 404) onSuccess(false)
                else onError("Não foi possível consultar o salvo (${response.code()}).")
            }
            override fun onFailure(call: Call<SavedItem>, error: Throwable) {
                onError("Não foi possível conectar ao servidor.")
            }
        })
    }

    fun save(userId: Long, localId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        mutate(ApiClient.service.saveLocal(userId, SavePlaceRequest(localId)), onSuccess, onError)
    }

    fun unsave(userId: Long, localId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        mutate(ApiClient.service.unsaveLocal(userId, localId), onSuccess, onError)
    }

    private fun mutate(call: Call<Void>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) onSuccess()
                else onError("Não foi possível atualizar o salvo (${response.code()}).")
            }
            override fun onFailure(call: Call<Void>, error: Throwable) {
                onError("Não foi possível conectar ao servidor.")
            }
        })
    }
}
