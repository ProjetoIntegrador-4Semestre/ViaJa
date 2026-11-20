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
        com.senac.viaja.data.remote.ApiClient.service.account().enqueue(object: Callback<com.senac.viaja.data.model.AccountProfile>{
            override fun onResponse(call:Call<com.senac.viaja.data.model.AccountProfile>,response:Response<com.senac.viaja.data.model.AccountProfile>){
                val profile=response.body();
                if(!response.isSuccessful||profile==null||profile.id!=id){
                    onError("Não foi possível carregar o perfil.");
                    return
                };
                ApiClient.service.saveAccount(com.senac.viaja.data.model.ProfileWrite(name,email,profile.defaultMode,profile.interestIds.toSet(),null)).enqueue(object:Callback<com.senac.viaja.data.model.ProfileSession>{
                    override fun onResponse(call:Call<com.senac.viaja.data.model.ProfileSession>,response:Response<com.senac.viaja.data.model.ProfileSession>){
                        if(response.isSuccessful)onSuccess()else onError("Use a página de perfil para confirmar a troca de e-mail.")
                    }
                    override fun onFailure(call:Call<com.senac.viaja.data.model.ProfileSession>,t:Throwable){
                        onError("Não foi possível salvar.")
                    }
                })
            }
            override fun onFailure(call:Call<com.senac.viaja.data.model.AccountProfile>,t:Throwable){
                onError("Não foi possível conectar ao servidor.")
            }
        })
    }
}
