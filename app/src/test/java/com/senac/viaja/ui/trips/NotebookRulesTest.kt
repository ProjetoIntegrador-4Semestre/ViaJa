package com.senac.viaja.ui.trips

import org.junit.Assert.*
import org.junit.Test

class NotebookRulesTest {
    @Test fun trimsAndRejectsWhitespaceOnly() {
        assertEquals("levar documento", NotebookRules.text("  levar documento\n", true))
        assertTrue(runCatching { NotebookRules.text(" \n ", false) }.isFailure)
    }

    @Test fun checklistHasItsOwnLengthLimit() {
        assertEquals(240, NotebookRules.text("x".repeat(240), true).length)
        assertTrue(runCatching { NotebookRules.text("x".repeat(241), true) }.isFailure)
    }

    @Test fun notesPreserveParagraphsAndHaveLargerLimit() {
        assertEquals("Hotel\n\nEndereço", NotebookRules.text("Hotel\n\nEndereço", false))
        assertEquals(4000, NotebookRules.text("x".repeat(4000), false).length)
        assertTrue(runCatching { NotebookRules.text("x".repeat(4001), false) }.isFailure)
    }
}
