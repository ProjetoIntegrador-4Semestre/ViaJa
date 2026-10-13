package com.senac.viaja.data.repository

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.data.mock.MockDataSource

class UserRepository {

    private val handler = Handler(Looper.getMainLooper())

    fun getById(
        id: Long,
        onSuccess: (ViajaUser) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            val user = MockDataSource.currentUser.copy(id = id)
            onSuccess(user)
        }, 600)
    }

    fun update(
        id: Long,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess()
        }, 600)
    }
}
