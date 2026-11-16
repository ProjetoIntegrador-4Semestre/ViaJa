package com.senac.viaja.ui.trips

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.model.TripBundle
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.main.journey
import com.senac.viaja.ui.main.press
import com.senac.viaja.ui.main.words

class OfflineTripsActivity : AppCompatActivity() {
    private lateinit var state: OfflineTripsState
    private lateinit var body: LinearLayout

    override fun onCreate(saved: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(saved)
        body = journey("Viagens baixadas")
        state = ViewModelProvider(this)[OfflineTripsState::class.java]
        if (state.selectedId == null) state.selectedId = saved?.getString("offline_trip")
        state.changed.observe(this) { render() }
        onBackPressedDispatcher.addCallback(this) {
            if (state.selectedId != null) {
                state.selectedId = null
                state.notifyChanged()
            } else finish()
        }
        if (!state.loaded) state.load()
    }

    override fun onSaveInstanceState(out: Bundle) {
        out.putString("offline_trip", state.selectedId)
        super.onSaveInstanceState(out)
    }

    override fun onResume() {
        super.onResume()
        if (::state.isInitialized && AuthRepository(this).offlineOwner() != state.owner) finish()
    }

    private fun render() {
        if (isFinishing || isDestroyed) return
        if (AuthRepository(this).offlineOwner() != state.owner) {
            finish()
            return
        }
        body.removeAllViews()
        body.words("Leitura offline", 24f)
        if (state.owner == null) {
            body.words("Entre na conta para consultar suas viagens baixadas.")
            body.press("Entrar na conta") { startActivity(Intent(this, LoginActivity::class.java)) }
            return
        }
        state.error?.let { body.words(it) }
        if (state.busy) body.words("Atualizando downloads…")
        if (state.error != null) body.press("Tentar novamente") { state.load() }.isEnabled = !state.busy
        val selected = state.items.firstOrNull { it.trip.id == state.selectedId }
        if (selected != null) {
            read(selected)
            return
        }
        if (state.loaded && state.items.isEmpty()) body.words("Nenhuma viagem baixada nesta conta.")
        state.items.forEach { bundle ->
            body.words(bundle.trip.name, 21f)
            body.words("Baixado em ${bundle.downloadedAt} · não inclui clima, mapa ou anexos offline", 12f)
            body.press("Ler viagem") {
                state.selectedId = bundle.trip.id
                state.notifyChanged()
            }.isEnabled = !state.busy
            body.press("Remover download") {
                MaterialAlertDialogBuilder(this).setTitle("Remover esta cópia offline?")
                    .setNegativeButton("Manter", null)
                    .setPositiveButton("Remover") { _, _ -> state.remove(bundle.trip.id) }.show()
            }.isEnabled = !state.busy
        }
    }

    private fun read(bundle: TripBundle) {
        body.words(bundle.trip.name, 24f)
        body.words(TripFormRules.datesLabel(bundle.trip.startDate, bundle.trip.endDate))
        body.words("Cópia de ${bundle.downloadedAt}. Alterações posteriores exigem novo download.", 12f)
        body.words("Roteiro", 21f)
        if (bundle.stops.isEmpty()) body.words("Nenhuma parada salva nesta cópia.")
        bundle.stops.forEach {
            body.words("Dia ${it.day} · ${it.time ?: "Sem horário"} · ${it.name}")
            it.address?.let { address -> body.words(address, 13f) }
            it.note?.let { note -> body.words(note) }
        }
        if (bundle.notes.isNotEmpty() || bundle.memos.isNotEmpty()) body.words("Anotações", 21f)
        bundle.notes.forEach { body.words(it.note.orEmpty()) }
        bundle.memos.forEach {
            body.words("${bundle.stops.firstOrNull { stop -> stop.localId == it.localId }?.name ?: "Parada"}: ${it.note.orEmpty()}")
        }
        if (bundle.checklist.isNotEmpty()) body.words("Checklist", 21f)
        bundle.checklist.forEach { body.words("${if (it.completed) "Concluído" else "Pendente"}: ${it.label}") }
        bundle.expenses?.let {
            body.words("Gastos: ${ExpenseRules.currency(it.totalCents)}", 21f)
            body.words(ExpenseRules.balanceLabel(it.remainingCents))
        }
        body.press("Voltar às viagens baixadas") {
            state.selectedId = null
            state.notifyChanged()
        }
    }
}
