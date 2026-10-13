package com.senac.viaja.data.repository

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.Subscription
import com.senac.viaja.data.mock.MockDataSource

class SubscriptionRepository {

    private val handler = Handler(Looper.getMainLooper())

    fun getByUser(
        userId: Long,
        onSuccess: (Subscription) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess(MockDataSource.subscription)
        }, 600)
    }
}
