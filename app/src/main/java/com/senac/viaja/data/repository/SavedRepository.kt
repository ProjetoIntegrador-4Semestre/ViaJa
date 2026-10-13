package com.senac.viaja.data.repository

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.SavedItem
import com.senac.viaja.data.mock.MockDataSource

class SavedRepository {

    private val handler = Handler(Looper.getMainLooper())

    fun list(
        userId: Long,
        onSuccess: (List<SavedItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            val items = MockDataSource.savedItems.filter { it.userId == userId.toString() }
            onSuccess(items)
        }, 600)
    }

    fun save(
        userId: Long,
        localId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess()
        }, 600)
    }

    fun unsave(
        userId: Long,
        localId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess()
        }, 600)
    }

    fun isSaved(userId: Long, localId: Long): Boolean {
        return MockDataSource.isSaved(userId.toString(), localId.toString())
    }
}
