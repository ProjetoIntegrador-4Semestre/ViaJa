package com.senac.viaja.ui.trips

import org.junit.Assert.*
import org.junit.Test

class TripFormRulesTest {
    @Test fun keepsUnknownBudgetDistinctFromZero() {
        assertNull(TripFormRules.validate("Rio", "", "", "").budgetCents)
        assertEquals(0L, TripFormRules.validate("Rio", "", "", "0,00").budgetCents)
    }

    @Test fun convertsBrazilianCurrencyWithoutRounding() {
        assertEquals(123456L, TripFormRules.validate("Rio", "", "", "1.234,56").budgetCents)
        assertEquals(1990L, TripFormRules.validate("Rio", "", "", "19,9").budgetCents)
        assertEquals("1234,56", TripFormRules.budgetInput(123456))
    }

    @Test fun rejectsMalformedNegativeAndOverLimitBudgets() {
        listOf("-10", "12,345", "1.23", "21.474.836,48").forEach {
            assertTrue("Orçamento inválido aceito: $it", runCatching { TripFormRules.validate("Rio", "", "", it) }.isFailure)
        }
        assertEquals(Int.MAX_VALUE.toLong(), TripFormRules.validate("Rio", "", "", "21.474.836,47").budgetCents)
    }

    @Test fun validatesLeapDaysAndCalendarDates() {
        assertEquals("2028-02-29", TripFormRules.validate("Rio", "29/02/2028", "", "").startDate)
        listOf("29/02/2026", "31/04/2026", "1/02/2026", "2026-02-01").forEach {
            assertTrue("Data inválida aceita: $it", runCatching { TripFormRules.validate("Rio", it, "", "") }.isFailure)
        }
    }

    @Test fun allowsOpenDatesButRejectsEndWithoutStartAndInvertedRange() {
        assertNull(TripFormRules.validate("Rio", "", "", "").startDate)
        assertNull(TripFormRules.validate("Rio", "01/12/2026", "", "").endDate)
        assertTrue(runCatching { TripFormRules.validate("Rio", "", "01/12/2026", "") }.isFailure)
        assertTrue(runCatching { TripFormRules.validate("Rio", "02/12/2026", "01/12/2026", "") }.isFailure)
    }

    @Test fun preventsDatesFromHidingExistingItineraryDays() {
        assertTrue(runCatching { TripFormRules.validate("Rio", "01/12/2026", "02/12/2026", "", 3) }.isFailure)
        assertEquals("2026-12-03", TripFormRules.validate("Rio", "01/12/2026", "03/12/2026", "", 3).endDate)
        assertEquals("2026-12-01", TripFormRules.validate("Rio", "01/12/2026", "01/12/2026", "", 1).endDate)
    }

    @Test fun trimsNamesAndRejectsEmptyOnes() {
        assertEquals("Rio", TripFormRules.validate("  Rio  ", "", "", "").name)
        assertTrue(runCatching { TripFormRules.validate("   ", "", "", "") }.isFailure)
    }
}
