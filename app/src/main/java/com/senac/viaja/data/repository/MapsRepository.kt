package com.senac.viaja.data.repository

import com.senac.viaja.data.model.GeocodeResponse
import com.senac.viaja.data.model.MapCoordinate
import com.senac.viaja.data.model.RouteRequest
import com.senac.viaja.data.model.RouteResponse
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsRepository {

    fun route(
        origin: MapCoordinate,
        destination: MapCoordinate,
        onSuccess: (RouteResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        ApiClient.service.route(RouteRequest(origin, destination)).enqueue(object : Callback<RouteResponse> {
            override fun onResponse(call: Call<RouteResponse>, response: Response<RouteResponse>) {
                val route = response.body()
                if (response.isSuccessful && route != null) onSuccess(route)
                else onError(if (response.code() == 404) "Nenhuma rota foi encontrada." else "Não foi possível calcular a rota (${response.code()}).")
            }

            override fun onFailure(call: Call<RouteResponse>, error: Throwable) {
                onError("Não foi possível conectar ao servidor para calcular a rota.")
            }
        })
    }

    fun geocode(address: String, onSuccess: (GeocodeResponse?) -> Unit, onError: () -> Unit) {
        ApiClient.service.geocode(address).enqueue(object : Callback<List<GeocodeResponse>> {
            override fun onResponse(call: Call<List<GeocodeResponse>>, response: Response<List<GeocodeResponse>>) {
                if (response.isSuccessful) onSuccess(response.body()?.firstOrNull()) else onError()
            }

            override fun onFailure(call: Call<List<GeocodeResponse>>, error: Throwable) = onError()
        })
    }
}
