package com.senac.viaja.ui.trips

import org.junit.Assert.*
import org.junit.Test

class ExpenseRulesTest {
    @Test fun parsesReaisAndKeepsOneCent() {
        val result = ExpenseRules.validate("  Café  ", "0,01", "18/11/2026", "food", "none")
        assertEquals(1L, result.amountCents)
        assertEquals("Café", result.description)
        assertEquals("2026-11-18", result.date)
    }

    @Test fun rejectsMissingZeroNegativeAndMalformedAmounts() {
        listOf("", "0", "-1", "10,999", "2.50").forEach {
            assertTrue(runCatching { ExpenseRules.validate("Almoço", it, "18/11/2026", "food", "none") }.isFailure)
        }
    }

    @Test fun requiresActualCalendarDateAndKnownEnums() {
        assertTrue(runCatching { ExpenseRules.validate("Hotel", "100", "31/02/2026", "lodging", "pending") }.isFailure)
        assertTrue(runCatching { ExpenseRules.validate("Hotel", "100", "", "lodging", "pending") }.isFailure)
        assertTrue(runCatching { ExpenseRules.validate("Hotel", "100", "18/11/2026", "unknown", "pending") }.isFailure)
        assertTrue(runCatching { ExpenseRules.validate("Hotel", "100", "18/11/2026", "lodging", "unknown") }.isFailure)
    }

    @Test fun preservesUnknownBudgetAndShowsOverspending() {
        assertNull(ExpenseRules.remaining(null, 100))
        assertEquals(-100L, ExpenseRules.remaining(0, 100))
        assertTrue(ExpenseRules.balanceLabel(-100).startsWith("Acima do orçamento:"))
        assertTrue(ExpenseRules.balanceLabel(null).startsWith("Defina um orçamento"))
    }

    @Test fun keepsTotalsAboveIntRangeExact() {
        val total = Int.MAX_VALUE.toLong() * 2
        assertEquals(-4294967294L, ExpenseRules.remaining(0, total))
        assertTrue(ExpenseRules.currency(total).contains("42.949.672,94"))
    }

    @Test fun rejectsBlankAndOverlongDescriptions() {
        assertTrue(runCatching { ExpenseRules.validate(" ", "10", "18/11/2026", "other", "none") }.isFailure)
        assertTrue(runCatching { ExpenseRules.validate("x".repeat(161), "10", "18/11/2026", "other", "none") }.isFailure)
    }
}
