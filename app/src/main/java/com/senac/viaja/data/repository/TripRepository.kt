package com.senac.viaja.data.repository

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.Trip
import com.senac.viaja.data.model.TripRequest
import com.senac.viaja.data.model.TripResponse
import com.senac.viaja.data.mock.MockDataSource

class TripRepository {

    private val handler = Handler(Looper.getMainLooper())

    fun list(
        onSuccess: (List<Trip>) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess(MockDataSource.trips)
        }, 600)
    }

    fun listByUser(
        userId: Long,
        onSuccess: (List<Trip>) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess(MockDataSource.trips)
        }, 600)
    }

    fun create(
        request: TripRequest,
        onSuccess: (TripResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            val newId = "t${MockDataSource.trips.size + 1}"
            val response = TripResponse(
                id = newId,
                userId = request.userId.toString(),
                name = request.name,
                mode = request.mode,
                startDate = request.startDate,
                endDate = request.endDate,
                totalBudgetCents = request.budgetCents,
                status = "ativa"
            )
            onSuccess(response)
        }, 600)
    }

    fun getById(
        id: Long,
        onSuccess: (Any) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            MockDataSource.getTripById(id.toString())?.let { onSuccess(it) }
                ?: onError("Viagem não encontrada")
        }, 600)
    }

    fun update(
        id: Long,
        request: TripRequest,
        onSuccess: (Any) -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess(TripResponse(
                id = id.toString(),
                userId = request.userId.toString(),
                name = request.name,
                mode = request.mode,
                startDate = request.startDate,
                endDate = request.endDate,
                totalBudgetCents = request.budgetCents,
                status = "atualizada"
            ))
        }, 600)
    }

    fun delete(
        id: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        handler.postDelayed({
            onSuccess()
        }, 600)
    }
}
