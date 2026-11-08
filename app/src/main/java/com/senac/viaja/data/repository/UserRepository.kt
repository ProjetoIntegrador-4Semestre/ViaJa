package com.senac.viaja.data.repository

import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository {
    fun getById(id: Long, onSuccess: (ViajaUser) -> Unit, onError: (String) -> Unit) {
        ApiClient.service.getUserById(id).enqueue(object : Callback<ViajaUser> {
            override fun onResponse(call: Call<ViajaUser>, response: Response<ViajaUser>) {
                val user = response.body()
                if (response.isSuccessful && user != null) onSuccess(user)
                else onError("Não foi possível carregar o perfil (${response.code()}).")
            }

            override fun onFailure(call: Call<ViajaUser>, error: Throwable) {
                onError("Não foi possível conectar ao servidor.")
            }
        })
    }

    fun update(id: Long, name: String, email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        onError("A atualização do perfil ainda não está disponível.")
    }
}
