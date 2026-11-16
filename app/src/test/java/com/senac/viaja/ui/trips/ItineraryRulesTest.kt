package com.senac.viaja.ui.trips

import com.senac.viaja.data.model.TripLocal
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ItineraryRulesTest {
    private fun stop(id: Long, day: Int, order: Int, time: String? = null) = TripLocal(1, id, day, order, "nota $id", time, id == 2L)

    @Test fun movingBetweenDaysClosesGapsAndPreservesData() {
        val first = stop(1, 1, 1, "08:30")
        val second = stop(2, 1, 2, "12:00")
        val third = stop(3, 2, 1)
        val moved = ItineraryRules.move(listOf(first, second, third), 2, 2, 0)
        assertEquals(listOf(1L, 2L, 3L), moved.map { it.localId })
        assertEquals(listOf(1, 1, 2), moved.map { it.orderIndex })
        assertEquals(second.copy(day = 2, orderIndex = 1), moved[1])
    }

    @Test fun reorderingDoesNotSilentlySortByTime() {
        val early = stop(1, 1, 1, "08:00")
        val late = stop(2, 1, 2, "18:00")
        val moved = ItineraryRules.move(listOf(early, late), 2, 1, 0)
        assertEquals(listOf(2L, 1L), moved.sortedWith(itineraryOrder).map { it.localId })
        assertEquals("18:00", moved.first().scheduledTime)
        assertTrue(moved.first().completed)
    }

    @Test fun removingAndNormalizingKeepsOtherDaysIndependent() {
        val normalized = ItineraryRules.normalize(listOf(stop(1, 1, 5), stop(3, 2, 9)))
        assertEquals(listOf(1, 1), normalized.map { it.orderIndex })
        assertEquals(listOf(1, 2), normalized.map { it.day })
    }

    @Test fun missingDatesDoNotInventDuration() {
        assertNull(ItineraryRules.duration(null, null))
        assertNull(ItineraryRules.duration("2026-12-01", null))
        assertEquals(3, ItineraryRules.duration("2028-02-28", "2028-03-01"))
        assertEquals(1, ItineraryRules.duration("2026-12-01", "2026-12-01"))
    }

    @Test fun validatesDaysAndCompositeKeyLimit() {
        assertTrue(runCatching { ItineraryRules.validate(listOf(stop(1, 3, 1)), 2) }.isFailure)
        assertTrue(runCatching { ItineraryRules.validate(listOf(stop(1, 1, 1), stop(1, 2, 1)), null) }.isFailure)
        ItineraryRules.validate(listOf(stop(1, 25, 1)), null)
    }

    @Test fun snapshotKeepsExpectedOrderInsteadOfNormalizingIt() {
        val entry = ItineraryRules.entries(listOf(stop(1, 2, 8, "12:30"))).single()
        assertEquals(8, entry.orderIndex)
        assertEquals("12:30", entry.scheduledTime)
        assertEquals("nota 1", entry.note)
    }

    @Test fun nextScheduledStopUsesClockWhileEditorUsesManualOrder() {
        val items = listOf(stop(3, 1, 1, "18:00"), stop(1, 1, 2, "09:00"))
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("2026-12-01 08:00")!!
        assertEquals(1L, previewStop(items, "2026-12-01", now).stop?.localId)
        assertEquals(3L, items.sortedWith(itineraryOrder).first().localId)
    }
}
