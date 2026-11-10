package com.senac.viaja.ui.main

import com.senac.viaja.data.model.Trip

internal fun selectHomeTrip(trips: List<Trip>, today: String): Trip? {
    val available = trips.filter {
        it.id?.toLongOrNull() != null && (it.status == "active" || it.status == "planned")
    }

    val current = available.filter { trip ->
        val start = trip.startDate
        val end = trip.endDate
        val hasStarted = start == null || start <= today
        val hasNotEnded = end == null || end >= today
        hasStarted && hasNotEnded &&
            (trip.status == "active" || (start != null && (start == today || end != null)))
    }
    val currentOrder = compareByDescending<Trip> { it.status == "active" }
        .thenByDescending { it.startDate }
        .thenBy { it.id }
    return current.minWithOrNull(currentOrder)
        ?: available.filter { it.startDate?.let { date -> date > today } == true }
            .minWithOrNull(compareBy<Trip> { it.startDate }.thenBy { it.id })
        ?: available.filter { it.status == "planned" && it.startDate == null && it.endDate == null }
            .minByOrNull { it.id.orEmpty() }
}
