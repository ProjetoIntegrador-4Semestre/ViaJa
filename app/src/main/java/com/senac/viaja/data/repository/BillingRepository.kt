package com.senac.viaja.data.repository

import com.senac.viaja.data.model.StripeHostedUrl
import com.senac.viaja.data.model.StripeSubscription
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BillingRepository {
    fun subscription(onSuccess: (StripeSubscription) -> Unit, onError: (String) -> Unit) =
        execute(ApiClient.service.getStripeSubscription(), onSuccess, onError)

    fun checkout(onSuccess: (StripeHostedUrl) -> Unit, onError: (String) -> Unit) =
        execute(ApiClient.service.startStripeCheckout(), onSuccess, onError)

    fun portal(onSuccess: (StripeHostedUrl) -> Unit, onError: (String) -> Unit) =
        execute(ApiClient.service.openStripePortal(), onSuccess, onError)

    private fun <T> execute(call: Call<T>, onSuccess: (T) -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    onSuccess(body)
                } else {
                    onError(when (response.code()) {
                        401 -> "Entre novamente para consultar sua assinatura."
                        409 -> "Já existe uma assinatura. Abra o portal para gerenciá-la."
                        503 -> "O Stripe de teste ainda não está configurado."
                        else -> "Não foi possível consultar a cobrança. Tente novamente."
                    })
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                onError("Sem conexão com a API de cobrança. Tente novamente.")
            }
        })
    }
}
