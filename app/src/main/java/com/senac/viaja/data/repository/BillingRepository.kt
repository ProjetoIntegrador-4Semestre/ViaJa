package com.senac.viaja.data.repository

import com.senac.viaja.data.model.StripeHostedUrl
import com.senac.viaja.data.model.StripeSubscription
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BillingRepository {
    fun subscription(onSuccess: (StripeSubscription) -> Unit, onError: (Int?) -> Unit) =
        execute(ApiClient.service.getStripeSubscription(), onSuccess, onError)

    fun hosted(checkout: Boolean, onSuccess: (StripeHostedUrl) -> Unit, onError: (Int?) -> Unit) =
        execute(
            if (checkout) ApiClient.service.startStripeCheckout() else ApiClient.service.openStripePortal(),
            onSuccess, onError
        )

    private fun <T> execute(call: Call<T>, onSuccess: (T) -> Unit, onError: (Int?) -> Unit): Call<T> {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (call.isCanceled) return
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body)
                else onError(response.code())
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                if (!call.isCanceled) onError(null)
            }
        })
        return call
    }
}
