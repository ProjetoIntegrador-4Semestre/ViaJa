package com.senac.viaja.ui.trips

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.ui.places.AiTripChatActivity
import java.text.NumberFormat
import java.util.Locale

class TripDashboardActivity : AppCompatActivity() {
    private lateinit var model: TripDashboardViewModel
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        val userId = AuthRepository(this).getUser()?.id ?: run {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        val tripId = intent.getStringExtra("trip_id")?.toLongOrNull()?.takeIf { it > 0 } ?: run { finish(); return }
        title = setPageContent(R.layout.activity_trip_dashboard, "Sua viagem", PageTab.TRIPS) { if (!::model.isInitialized || !model.mutating) finish() }
        model = ViewModelProvider(this)[TripDashboardViewModel::class.java]
        model.tripId = tripId
        model.ownerId = userId
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (!model.mutating) finish() }
        })
        model.changed.observe(this) { render() }
        findViewById<View>(R.id.tripDashboardRetry).setOnClickListener { model.refresh() }
        findViewById<View>(R.id.tripDashboardStopRetry).setOnClickListener { model.refresh() }
        findViewById<View>(R.id.tripDashboardEdit).setOnClickListener {
            startActivity(Intent(this, TripEditorActivity::class.java).putExtra(TripEditorActivity.TRIP_ID, tripId))
        }
        findViewById<View>(R.id.btnOpenTripItinerary).setOnClickListener {
            startActivity(Intent(this, TripItineraryActivity::class.java).putExtra(TripItineraryActivity.TRIP_ID, tripId))
        }
        findViewById<View>(R.id.tripDashboardResources).setOnClickListener {
            if(isOwner()) startActivity(Intent(this,TripResourcesActivity::class.java).putExtra(TripResourcesActivity.TRIP_ID,tripId)) else finish()
        }
        findViewById<View>(R.id.tripDashboardExpenses).setOnClickListener {
            if (isOwner()) startActivity(Intent(this, TripExpensesActivity::class.java).putExtra(TripExpensesActivity.TRIP_ID, tripId)) else finish()
        }
        findViewById<View>(R.id.tripDashboardNotebook).setOnClickListener {
            if (isOwner()) startActivity(Intent(this, TripNotebookActivity::class.java).putExtra(TripNotebookActivity.TRIP_ID, tripId)) else finish()
        }
        findViewById<View>(R.id.tripDashboardChat).setOnClickListener { openChat() }
        findViewById<View>(R.id.pageAi).setOnClickListener { openChat() }
        findViewById<View>(R.id.tripDashboardDelete).setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Excluir ${model.trip?.name ?: "viagem"}?")
                .setMessage("A viagem, suas paradas, notas e anexos vinculados serão excluídos. Esta ação não pode ser desfeita.")
                .setNegativeButton("Manter viagem", null).setPositiveButton("Excluir") { _, _ ->
                    if (isOwner()) model.delete() else finish()
                }.show()
        }
        findViewById<View>(R.id.tripDashboardStatus).setOnClickListener { selectStatus() }
    }

    override fun onResume() {
        super.onResume()
        if (!::model.isInitialized) return
        if (!isOwner()) { finish(); return }
        model.refresh()
    }

    private fun isOwner() = AuthRepository(this).getUser()?.id == model.ownerId

    private fun render() {
        if (isFinishing || isDestroyed) return
        if (model.deleted) { setResult(RESULT_OK); finish(); return }
        val menu = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav).menu
        for (index in 0 until menu.size()) menu.getItem(index).isEnabled = !model.mutating
        findViewById<View>(R.id.pageBack).isEnabled = !model.mutating
        val trip = model.trip
        findViewById<TextView>(R.id.tripDashboardExpenseSummary).text = model.expenseSummary
        val available = trip != null && !model.loading && !model.mutating
        title.text = trip?.name ?: "Sua viagem"
        findViewById<TextView>(R.id.tvTripDestination).text = trip?.name ?: "Carregando viagem…"
        findViewById<TextView>(R.id.tvTripDates).text = trip?.let { TripFormRules.datesLabel(it.startDate, it.endDate) }.orEmpty()
        findViewById<TextView>(R.id.tvTripMode).text = trip?.let { if (it.mode == "work") "TRABALHO" else "TURISMO" }.orEmpty()
        findViewById<TextView>(R.id.tripDashboardCity).text = model.city
        findViewById<TextView>(R.id.tripDashboardBudget).text = trip?.totalBudgetCents?.let {
            "Orçamento · ${NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(it / 100.0)}"
        } ?: if (trip == null) "" else "Orçamento a definir"
        findViewById<TextView>(R.id.tripDashboardState).text = when (trip?.status) {
            "active" -> "Em andamento"
            "completed" -> "Concluída · seu roteiro continua disponível"
            "cancelled" -> "Cancelada · seu planejamento foi preservado"
            "planned" -> "Planejada"
            else -> ""
        }
        findViewById<TextView>(R.id.tripDashboardError).apply {
            text = model.error ?: when {
                model.mutating -> "Salvando alteração…"
                model.loading -> "Atualizando viagem…"
                else -> ""
            }
            isVisible = text.isNotBlank()
        }
        findViewById<View>(R.id.tripDashboardRetry).isVisible = model.error != null && !model.loading && !model.mutating
        findViewById<TextView>(R.id.tvNextStop).text = model.stopTitle
        findViewById<TextView>(R.id.tvNextStopName).apply { text = model.stopName; isVisible = text.isNotBlank() }
        findViewById<TextView>(R.id.tvNextStopTime).apply { text = model.stopTime; isVisible = text.isNotBlank() }
        findViewById<TextView>(R.id.tripDashboardProgress).text = model.progress
        findViewById<View>(R.id.tripDashboardStopRetry).isVisible = model.stopError != null
        findViewById<TextView>(R.id.tripDashboardPending).text = trip?.let {
            val pending = mutableListOf<String>()
            if (it.startDate == null || it.endDate == null) pending.add("Defina as datas quando estiver pronto.")
            if (it.totalBudgetCents == null) pending.add("Um orçamento ajuda a orientar suas escolhas.")
            pending.joinToString("\n")
        }.orEmpty()
        listOf(R.id.tripDashboardEdit, R.id.tripDashboardDelete, R.id.tripDashboardStatus,
            R.id.btnOpenTripItinerary, R.id.tripDashboardResources, R.id.tripDashboardExpenses, R.id.tripDashboardNotebook, R.id.tripDashboardChat, R.id.pageAi).forEach { findViewById<View>(it).isEnabled = available }
    }

    private fun openChat() {
        val trip = model.trip ?: return
        if (!isOwner()) { finish(); return }
        startActivity(Intent(this, AiTripChatActivity::class.java).putExtra(PAGE_TAB_EXTRA, R.id.nav_trips)
            .putExtra("trip_id", model.tripId).putExtra("trip_name", trip.name).putExtra("trip_mode", trip.mode))
    }

    private fun selectStatus() {
        val options = listOf("planned" to "Planejada", "active" to "Em andamento", "completed" to "Concluída", "cancelled" to "Cancelada")
            .filterNot { it.first == model.trip?.status }
        MaterialAlertDialogBuilder(this).setTitle("Estado da viagem")
            .setItems(options.map { it.second }.toTypedArray()) { _, index ->
                val option = options[index]
                MaterialAlertDialogBuilder(this).setTitle("Marcar como ${option.second.lowercase(Locale.ROOT)}?")
                    .setMessage("Seu roteiro e suas informações serão preservados.")
                    .setNegativeButton("Manter", null).setPositiveButton("Confirmar") { _, _ ->
                        if (isOwner()) model.updateStatus(option.first) else finish()
                    }.show()
            }.setNegativeButton("Fechar", null).show()
    }
}
