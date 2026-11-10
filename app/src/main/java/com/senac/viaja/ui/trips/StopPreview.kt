package com.senac.viaja.ui.trips

import com.senac.viaja.data.model.TripLocal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal data class StopPreview(val title: String, val stop: TripLocal? = null)

private val clockPattern = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")

internal val TripLocal.displayTime: String?
    get() = scheduledTime?.takeIf { clockPattern.matches(it) }

internal val itineraryOrder = compareBy<TripLocal> { it.day ?: 1 }
    .thenBy { it.displayTime ?: "24:00" }
    .thenBy { it.orderIndex ?: Int.MAX_VALUE }
    .thenBy { it.localId }

internal fun previewStop(stops: List<TripLocal>, startDate: String?, now: Date = Date()): StopPreview {
    if (stops.isEmpty()) return StopPreview("Seu roteiro está vazio")
    val pending = stops.filterNot { it.completed }.sortedWith(itineraryOrder)
    if (pending.isEmpty()) return StopPreview("Todas as paradas foram concluídas")

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    val start = startDate?.let { runCatching { dateFormat.parse(it) }.getOrNull() }
        ?: return StopPreview("Primeira parada pendente", pending.first())
    val today = dateFormat.format(now)
    val currentTime = SimpleDateFormat("HH:mm", Locale.US).format(now)
    val byDate = pending.map { stop ->
        val date = Calendar.getInstance().apply {
            time = start
            add(Calendar.DATE, (stop.day ?: 1) - 1)
        }
        stop to dateFormat.format(date.time)
    }
    val next = byDate.firstOrNull { (stop, date) ->
        stop.displayTime?.let { date > today || (date == today && it >= currentTime) } == true
    }
    if (next != null) return StopPreview("Próxima parada", next.first)
    val untimed = byDate.firstOrNull { (stop, date) -> date >= today && stop.displayTime == null }
    if (untimed != null) return StopPreview("Parada sem horário", untimed.first)
    return StopPreview("Nenhuma parada futura")
}
