package com.senac.viaja.data.repository

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.City
import com.senac.viaja.data.mock.MockDataSource

class CityRepository {

    private val handler = Handler(Looper.getMainLooper())

    fun list(
        onSuccess: (List<City>) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess(MockDataSource.cities)
        }, 600)
    }
}
