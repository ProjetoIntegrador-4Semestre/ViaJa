package com.senac.viaja.ui.trips

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.model.ItineraryPlace
import com.senac.viaja.data.model.TripLocal
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.ui.places.AiTripChatActivity
import com.senac.viaja.ui.places.PlaceDetailsActivity
import java.util.Locale

class TripItineraryActivity : AppCompatActivity() {
    private lateinit var model: ItineraryViewModel
    private lateinit var title: TextView
    private var restoredDraft: Bundle? = null
    private var catalogDialog: androidx.appcompat.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        val owner = AuthRepository(this).getUser()?.id ?: run { finish(); return }
        val id = intent.getLongExtra(TRIP_ID, -1L).takeIf { it > 0 } ?: run { finish(); return }
        title = setPageContent(R.layout.activity_trip_itinerary, "Roteiro", null) { closeEditor() }
        model = ViewModelProvider(this)[ItineraryViewModel::class.java]
        model.tripId = id
        if (!model.ready && savedInstanceState?.getLong("draft_owner") == owner) restoredDraft = savedInstanceState
        if (model.ownerId == 0L) model.ownerId = owner
        if (model.incomingPlaceId == null && !model.ready) model.incomingPlaceId = intent.getLongExtra(ADD_PLACE_ID, -1L).takeIf { it > 0 }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closeEditor()
        })
        model.changed.observe(this) { render() }
        findViewById<View>(R.id.itineraryRetry).setOnClickListener { reload() }
        findViewById<View>(R.id.itinerarySave).setOnClickListener { if (isOwner()) model.save() else finish() }
        findViewById<View>(R.id.itineraryPrevious).setOnClickListener { model.selectDay(model.selectedDay - 1) }
        findViewById<View>(R.id.itineraryNext).setOnClickListener { model.selectDay(model.selectedDay + 1) }
        findViewById<View>(R.id.itinerarySelectDay).setOnClickListener { chooseDay(model.selectedDay) { model.selectDay(it) } }
        findViewById<View>(R.id.itineraryAdd).setOnClickListener { if (isOwner()) model.loadCatalog() else finish() }
        findViewById<View>(R.id.pageAi).setOnClickListener {
            val trip = model.trip ?: return@setOnClickListener
            if (model.dirty) { Toast.makeText(this, "Salve ou descarte os ajustes antes de abrir a Vi.", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            startActivity(Intent(this, AiTripChatActivity::class.java).putExtra("trip_id", id)
                .putExtra("trip_name", trip.name).putExtra("trip_mode", trip.mode))
        }
        if (!model.ready && !model.loading) model.load()
    }

    override fun onResume() {
        super.onResume()
        if (::model.isInitialized && !isOwner()) finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::model.isInitialized && model.ready) {
            val gson = com.google.gson.Gson()
            outState.putLong("draft_owner", model.ownerId)
            outState.putString("draft_stops", gson.toJson(model.draft))
            outState.putString("original_stops", gson.toJson(model.original))
            outState.putInt("selected_day", model.selectedDay)
            outState.putString("place_names", gson.toJson(model.names))
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        catalogDialog?.dismiss()
        super.onDestroy()
    }

    private fun isOwner() = AuthRepository(this).getUser()?.id == model.ownerId

    private fun render() {
        if (isFinishing || isDestroyed) return
        if (model.ready) restoredDraft?.let { saved ->
            restoredDraft = null
            runCatching {
                val type = object : com.google.gson.reflect.TypeToken<List<TripLocal>>() {}.type
                val gson = com.google.gson.Gson()
                val draft = gson.fromJson<List<TripLocal>>(saved.getString("draft_stops"), type)
                val original = gson.fromJson<List<TripLocal>>(saved.getString("original_stops"), type)
                if (draft != null && original != null && draft != original) {
                    model.draft = draft
                    model.original = original
                    model.selectedDay = saved.getInt("selected_day", 1).coerceAtLeast(1)
                    val namesType = object : com.google.gson.reflect.TypeToken<Map<Long, String>>() {}.type
                    gson.fromJson<Map<Long, String>>(saved.getString("place_names"), namesType)?.let { model.names.putAll(it) }
                }
            }
        }
        val busy = model.loading || model.saving
        title.text = model.trip?.name ?: "Roteiro"
        findViewById<TextView>(R.id.itineraryTripName).text = model.trip?.let { TripFormRules.datesLabel(it.startDate, it.endDate) }.orEmpty()
        findViewById<View>(R.id.itineraryLoading).isVisible = busy || model.catalogLoading
        findViewById<TextView>(R.id.itineraryStatus).apply {
            text = model.error ?: when {
                model.saving -> "Salvando roteiro…"
                model.loading -> "Carregando roteiro…"
                model.catalogLoading -> "Consultando lugares do destino…"
                model.dirty -> "Alterações ainda não salvas."
                else -> model.notice.orEmpty()
            }
            isVisible = text.isNotBlank()
        }
        findViewById<View>(R.id.itineraryRetry).isVisible = !busy && (model.error != null || model.ready)
        findViewById<TextView>(R.id.itineraryAvailability).text = when {
            !model.ready -> ""
            !model.fullEditing -> "Edição completa indisponível no momento. Horário e conclusão continuam disponíveis."
            model.duration == null -> "Sem duração definida: os dias são posições no roteiro. Salve para aplicar seus ajustes."
            else -> "${model.duration} dias de viagem. Ajuste a ordem sem alterar os horários das paradas."
        } + if (model.incomingPlaceId != null) " Escolha o dia e toque em Adicionar lugar para incluir o local selecionado." else ""
        findViewById<TextView>(R.id.itinerarySelectDay).text = ItineraryRules.dayLabel(model.selectedDay, model.trip?.startDate)
        findViewById<View>(R.id.itineraryPrevious).isEnabled = model.ready && !busy && model.selectedDay > 1
        findViewById<View>(R.id.itineraryNext).isEnabled = model.ready && !busy && (model.duration == null || model.selectedDay < model.dayLimit)
        findViewById<View>(R.id.itinerarySelectDay).isEnabled = model.ready && !busy
        findViewById<View>(R.id.itineraryAdd).isEnabled = model.ready && model.fullEditing && !busy && !model.catalogLoading
        findViewById<View>(R.id.itinerarySave).apply { isVisible = model.fullEditing; isEnabled = model.dirty && !busy }
        findViewById<View>(R.id.pageAi).isEnabled = model.ready && !busy
        val rows = findViewById<LinearLayout>(R.id.itineraryDays)
        rows.removeAllViews()
        if (model.ready) {
            val selected = model.draft.filter { it.day == model.selectedDay }.sortedWith(itineraryOrder)
            if (selected.isEmpty()) rows.addView(TextView(this).apply {
                text = "Este dia está livre. Adicione um lugar para começar."
                setTextColor(getColor(R.color.vi_muted))
                setPadding(8, 32, 8, 32)
            })
            selected.forEachIndexed { index, stop ->
                val row = layoutInflater.inflate(R.layout.item_itinerary_stop, rows, false)
                row.findViewById<TextView>(R.id.itineraryPlaceName).text = model.names[stop.localId] ?: "Carregando lugar…"
                row.findViewById<TextView>(R.id.itineraryPlaceTime).text =
                    "${index + 1}. ${stop.displayTime ?: "Sem horário"}${if (stop.completed) " · Concluída" else ""}"
                row.findViewById<TextView>(R.id.itineraryPlaceNote).apply { text = stop.note; isVisible = !text.isNullOrBlank() }
                row.findViewById<View>(R.id.itinerarySchedule).apply {
                    isEnabled = !busy
                    setOnClickListener { if (isOwner()) actions(stop) else finish() }
                }
                row.findViewById<View>(R.id.itineraryPlaceName).setOnClickListener {
                    if (model.dirty) { Toast.makeText(this, "Salve ou descarte os ajustes antes de sair do roteiro.", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    startActivity(Intent(this, PlaceDetailsActivity::class.java).putExtra("place_id", stop.localId.toString()).putExtra(PAGE_TAB_EXTRA, PageTab.TRIPS.menuId))
                }
                rows.addView(row)
            }
        }
        if (model.showCatalog && !busy) {
            model.showCatalog = false
            showPlaces()
        }
        if (!model.dirty) setResult(RESULT_OK)
    }

    private fun showPlaces() {
        val incoming = model.incomingPlaceId
        if (incoming != null && model.catalog.none { it.id == incoming }) {
            MaterialAlertDialogBuilder(this).setTitle("Outro destino")
                .setMessage("O lugar selecionado não está disponível no destino desta viagem. Escolha outra viagem para incluí-lo.")
                .setPositiveButton("Entendi", null).show()
            return
        }
        if (incoming != null && model.draft.any { it.localId == incoming }) {
            Toast.makeText(this, "O lugar selecionado já está no roteiro. Use Ajustar parada para movê-lo.", Toast.LENGTH_LONG).show()
        }
        val available = model.catalog.filterNot { place -> model.draft.any { it.localId == place.id } }
            .sortedBy { if (it.id == incoming) 0 else 1 }
        if (available.isEmpty()) {
            Toast.makeText(this, "Nenhum lugar novo disponível para este destino.", Toast.LENGTH_LONG).show()
            return
        }
        catalogDialog = MaterialAlertDialogBuilder(this).setTitle("Adicionar ao roteiro")
            .setItems(available.map { if (it.id == incoming) "${it.name} · Selecionado" else it.name }.toTypedArray()) { _, index ->
                val place = available[index]
                chooseDay(model.selectedDay) { day ->
                    MaterialAlertDialogBuilder(this).setTitle("Horário de ${place.name}")
                        .setItems(arrayOf("Sem horário", "Definir horário")) { _, option ->
                            if (option == 0) model.add(place, day, null)
                            else pickTime(null) { model.add(place, day, it) }
                        }.setNegativeButton("Cancelar", null).show()
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun actions(stop: TripLocal) {
        val options = mutableListOf("Definir horário")
        if (stop.displayTime != null) options.add("Remover horário")
        options.add(if (stop.completed) "Reabrir parada" else "Marcar como concluída")
        options.add("Anotações desta parada")
        if (model.fullEditing) options.addAll(listOf("Mover para outro dia", "Alterar posição", "Editar nota da parada", "Remover do roteiro"))
        MaterialAlertDialogBuilder(this).setTitle(model.names[stop.localId] ?: "Parada")
            .setItems(options.toTypedArray()) { _, index ->
                when (options[index]) {
                    "Definir horário" -> pickTime(stop.displayTime) { model.edit(stop, time = it) }
                    "Remover horário" -> model.edit(stop, time = null)
                    "Marcar como concluída", "Reabrir parada" -> model.edit(stop, completed = !stop.completed)
                    "Mover para outro dia" -> chooseDay(stop.day ?: 1) { model.edit(stop, day = it) }
                    "Alterar posição" -> {
                        val count = model.draft.count { it.day == stop.day }
                        MaterialAlertDialogBuilder(this).setTitle("Nova posição")
                            .setItems((1..count).map { "Posição $it" }.toTypedArray()) { _, position -> model.edit(stop, position = position) }
                            .setNegativeButton("Cancelar", null).show()
                    }
                    "Anotações desta parada" -> {
                        if (model.dirty) {
                            Toast.makeText(this, "Salve ou descarte os ajustes do roteiro antes de abrir as anotações.", Toast.LENGTH_LONG).show()
                        } else startActivity(Intent(this, TripNotebookActivity::class.java)
                            .putExtra(TripNotebookActivity.TRIP_ID, model.tripId)
                            .putExtra(TripNotebookActivity.LOCAL_ID, stop.localId ?: -1L)
                            .putExtra(TripNotebookActivity.CONTEXT_NAME, model.names[stop.localId].orEmpty()))
                    }
                    "Editar nota da parada" -> {
                        val input = EditText(this).apply { setText(stop.note); hint = "Informações desta parada"; maxLines = 6 }
                        MaterialAlertDialogBuilder(this).setTitle("Nota da parada").setView(input)
                            .setNegativeButton("Cancelar", null).setPositiveButton("Aplicar ao rascunho") { _, _ ->
                                model.edit(stop, note = input.text.toString().trim().ifBlank { null })
                            }.show()
                    }
                    "Remover do roteiro" -> MaterialAlertDialogBuilder(this).setTitle("Remover esta parada?")
                        .setMessage("Ao salvar o roteiro, a parada e suas anotações vinculadas serão excluídas.")
                        .setNegativeButton("Manter", null).setPositiveButton("Remover do rascunho") { _, _ -> model.remove(stop) }.show()
                }
            }.setNegativeButton("Fechar", null).show()
    }

    private fun chooseDay(initial: Int, action: (Int) -> Unit) {
        val field = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER; setText(initial.toString()); selectAll() }
        val dialog = MaterialAlertDialogBuilder(this).setTitle("Dia do roteiro")
            .setMessage(model.duration?.let { "Escolha de 1 a $it." } ?: "Informe a posição do dia. Isso não define a duração da viagem.")
            .setView(field).setNegativeButton("Cancelar", null).setPositiveButton("Escolher", null).create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val day = field.text.toString().toIntOrNull()
                if (day == null || day < 1 || day > (model.duration ?: 10000)) field.error = "Informe um dia válido${model.duration?.let { " de 1 a $it" }.orEmpty()}."
                else { dialog.dismiss(); action(day) }
            }
        }
        dialog.show()
    }

    private fun pickTime(initial: String?, action: (String) -> Unit) {
        val parts = initial?.split(':')
        TimePickerDialog(this, { _, hour, minute -> action(String.format(Locale.US, "%02d:%02d", hour, minute)) },
            parts?.getOrNull(0)?.toIntOrNull() ?: 9, parts?.getOrNull(1)?.toIntOrNull() ?: 0, true).show()
    }

    private fun reload() {
        if (!model.dirty) { model.load(); return }
        MaterialAlertDialogBuilder(this).setTitle("Recarregar e descartar ajustes?")
            .setMessage("O roteiro salvo será consultado novamente. Os ajustes desta tela serão descartados.")
            .setNegativeButton("Continuar editando", null).setPositiveButton("Recarregar") { _, _ -> model.load() }.show()
    }

    private fun closeEditor() {
        if (!::model.isInitialized) { finish(); return }
        if (model.saving) return
        if (!model.dirty) { finish(); return }
        MaterialAlertDialogBuilder(this).setTitle("Sair sem salvar?")
            .setMessage("Seus ajustes ainda não foram aplicados ao roteiro.")
            .setNegativeButton("Continuar editando", null).setPositiveButton("Descartar") { _, _ -> finish() }.show()
    }

    companion object {
        const val TRIP_ID = "itinerary_trip_id"
        const val ADD_PLACE_ID = "itinerary_add_place_id"
    }
}
