package com.senac.viaja.data.repository

import com.senac.viaja.data.model.City
import com.senac.viaja.data.model.CityPage
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CityRepository {
    fun getById(id: Long, onSuccess: (City) -> Unit, onError: () -> Unit) {
        ApiClient.service.getCityById(id).enqueue(object : Callback<City> {
            override fun onResponse(call: Call<City>, response: Response<City>) {
                val city = response.body()
                if (response.isSuccessful && city != null) onSuccess(city) else onError()
            }

            override fun onFailure(call: Call<City>, error: Throwable) {
                onError()
            }
        })
    }

    fun list(onSuccess: (List<City>) -> Unit, onError: (String) -> Unit) = loadPage(0, mutableListOf(), onSuccess, onError)

    private fun loadPage(page: Int, items: MutableList<City>, onSuccess: (List<City>) -> Unit, onError: (String) -> Unit) {
        ApiClient.service.getCities(page).enqueue(object : Callback<CityPage> {
            override fun onResponse(call: Call<CityPage>, response: Response<CityPage>) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    onError("Não foi possível carregar os destinos (${response.code()}).")
                    return
                }
                items.addAll(body.embedded?.cityList.orEmpty())
                if (page + 1 < body.page.totalPages) loadPage(page + 1, items, onSuccess, onError)
                else onSuccess(items)
            }
            override fun onFailure(call: Call<CityPage>, error: Throwable) { onError("Não foi possível conectar ao servidor.") }
        })
    }
}
