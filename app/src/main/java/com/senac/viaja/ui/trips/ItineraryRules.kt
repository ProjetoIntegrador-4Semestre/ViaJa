package com.senac.viaja.ui.trips

import com.senac.viaja.data.model.ItineraryEntry
import com.senac.viaja.data.model.TripLocal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

internal object ItineraryRules {
    fun duration(start: String?, end: String?): Int? {
        if (start == null || end == null) return null
        return runCatching {
            val format = isoFormat()
            ((format.parse(end)!!.time - format.parse(start)!!.time) / 86_400_000L + 1).toInt().takeIf { it > 0 }
        }.getOrNull()
    }

    private fun isoFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun dayLabel(day: Int, start: String?): String {
        val date = start?.let { value -> runCatching {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = isoFormat().parse(value)!!
                add(Calendar.DATE, day - 1)
            }
            SimpleDateFormat("dd/MM", Locale.forLanguageTag("pt-BR")).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(calendar.time)
        }.getOrNull() }
        return "Dia $day" + if (date == null) "" else " · $date"
    }

    fun entries(stops: List<TripLocal>) = stops.map {
        ItineraryEntry(requireNotNull(it.localId), requireNotNull(it.day), requireNotNull(it.orderIndex), it.note, it.displayTime, it.completed)
    }

    fun normalize(stops: List<TripLocal>): List<TripLocal> = stops.groupBy { it.day ?: 1 }.toSortedMap().flatMap { (day, items) ->
        items.mapIndexed { index, stop -> stop.copy(day = day, orderIndex = index + 1) }
    }

    fun move(stops: List<TripLocal>, localId: Long, day: Int, position: Int): List<TripLocal> {
        require(day > 0) { "Escolha um dia válido." }
        val target = stops.first { it.localId == localId }
        val others = stops.filterNot { it.localId == localId }.sortedWith(itineraryOrder)
        val destination = others.filter { it.day == day }.toMutableList()
        require(position in 0..destination.size) { "Escolha uma posição válida." }
        destination.add(position, target.copy(day = day))
        return normalize(others.filterNot { it.day == day } + destination)
    }

    fun validate(stops: List<TripLocal>, duration: Int?) {
        require(stops.all { it.localId != null && it.day != null && it.day > 0 && (duration == null || it.day <= duration) }) { "Existem paradas fora dos dias da viagem." }
        require(stops.map { it.localId }.distinct().size == stops.size) { "Este lugar já está no roteiro." }
    }
}
