package com.senac.viaja.ui.trips

internal object NotebookRules {
    fun text(raw: String, checklist: Boolean): String {
        val value = raw.trim()
        require(value.isNotEmpty()) { if (checklist) "Descreva o item do checklist." else "Escreva a anotação antes de salvar." }
        val max = if (checklist) 240 else 4000
        require(value.length <= max) { "Use até $max caracteres." }
        return value
    }
}
