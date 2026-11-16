package com.senac.viaja.ui.trips

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.main.setPageContent

class TripNotebookActivity : AppCompatActivity() {
    private lateinit var model: TripNotebookViewModel
    private lateinit var input: EditText
    private var boundVersion = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        val owner = AuthRepository(this).getUser()?.id ?: run { finish(); return }
        val id = intent.getLongExtra(TRIP_ID, -1L).takeIf { it > 0 } ?: run { finish(); return }
        setPageContent(R.layout.activity_trip_notebook, "Notas e checklist", null) { closePage() }
        model = ViewModelProvider(this)[TripNotebookViewModel::class.java]
        val newModel = model.ownerId == 0L
        model.tripId = id
        if (newModel) {
            model.ownerId = owner
            model.localId = intent.getLongExtra(LOCAL_ID, -1L).takeIf { it > 0 }
            model.contextName = intent.getStringExtra(CONTEXT_NAME).orEmpty()
            if (savedInstanceState?.getLong("draft_owner") == owner) restoreDraft(savedInstanceState)
        }
        input = findViewById(R.id.notebookInput)
        input.doAfterTextChanged {
            model.editorText = it.toString()
            showCounter()
        }
        model.changed.observe(this) { render() }
        findViewById<View>(R.id.notebookAddNote).setOnClickListener { if (isOwner()) model.openNote() else finish() }
        findViewById<View>(R.id.notebookAddItem).setOnClickListener { if (isOwner()) model.openItem() else finish() }
        findViewById<View>(R.id.notebookSave).setOnClickListener { if (isOwner()) model.saveEditor() else finish() }
        findViewById<View>(R.id.notebookCancel).setOnClickListener { discardEditor { model.cancelEditor() } }
        findViewById<View>(R.id.notebookReload).setOnClickListener { if (isOwner()) model.load() else finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closePage()
        })
        if (!model.notesReady && !model.notesLoading) model.load()
    }

    override fun onResume() {
        super.onResume()
        if (::model.isInitialized && !isOwner()) finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::model.isInitialized) {
            outState.putLong("draft_owner", model.ownerId)
            outState.putBoolean("editor_visible", model.editorVisible)
            outState.putBoolean("editor_checklist", model.editorChecklist)
            outState.putString("editor_id", model.editorId)
            outState.putString("editor_request", model.editorRequestId)
            outState.putString("editor_text", model.editorText)
            outState.putString("editor_original", model.editorOriginal)
            outState.putBoolean("editor_completed", model.editorCompleted)
        }
        super.onSaveInstanceState(outState)
    }

    private fun restoreDraft(state: Bundle) {
        model.editorVisible = state.getBoolean("editor_visible")
        model.editorChecklist = state.getBoolean("editor_checklist")
        model.editorId = state.getString("editor_id")
        model.editorRequestId = state.getString("editor_request") ?: model.editorRequestId
        model.editorText = state.getString("editor_text").orEmpty()
        model.editorOriginal = state.getString("editor_original").orEmpty()
        model.editorCompleted = state.getBoolean("editor_completed")
    }

    private fun isOwner() = AuthRepository(this).getUser()?.id == model.ownerId

    private fun showCounter() {
        findViewById<TextView>(R.id.notebookCounter).text = "${input.length()} / ${if (model.editorChecklist) 240 else 4000} caracteres"
    }

    private fun render() {
        if (isFinishing || isDestroyed) return
        if (!isOwner()) { finish(); return }
        findViewById<TextView>(R.id.notebookSubtitle).text = if (model.localId == null)
            "${model.tripName}\nSuas anotações e preparativos em um lugar."
        else "${model.contextName.ifBlank { "Parada selecionada" }}\nAnotações desta parada · checklist da viagem inteira."
        findViewById<TextView>(R.id.notebookStatus).apply {
            text = model.mutationError ?: if (model.busy) "Salvando alteração…" else model.notice.orEmpty()
            isVisible = text.isNotBlank()
        }
        findViewById<View>(R.id.notebookEditor).isVisible = model.editorVisible
        if (model.editorVisible && model.editorVersion != boundVersion) {
            boundVersion = model.editorVersion
            input.filters = arrayOf(InputFilter.LengthFilter(if (model.editorChecklist) 240 else 4000))
            input.setText(model.editorText)
            input.setSelection(input.length())
        }
        findViewById<TextView>(R.id.notebookEditorTitle).text = if (model.editorChecklist) "Item do checklist" else "Anotação"
        input.isEnabled = !model.busy
        showCounter()
        findViewById<View>(R.id.notebookSave).isEnabled = !model.busy && (!model.editorChecklist || model.checklistSupported)
        findViewById<View>(R.id.notebookCancel).isEnabled = !model.busy
        findViewById<View>(R.id.notebookReload).isEnabled = !model.busy && !model.notesLoading && !model.checklistLoading
        findViewById<View>(R.id.notebookAddNote).isEnabled = !model.busy && !model.editorVisible && model.notesReady && !model.notesLoading
        findViewById<View>(R.id.notebookAddItem).isEnabled = !model.busy && !model.editorVisible && model.checklistSupported && !model.checklistLoading
        findViewById<TextView>(R.id.notebookNotesTitle).text = if (model.localId == null) "Anotações da viagem" else "Anotações da parada"
        findViewById<TextView>(R.id.notebookNotesState).text = when {
            model.notesLoading -> "Carregando anotações…"
            model.notesError != null -> model.notesError
            model.notes.isEmpty() -> "Guarde endereços, lembretes e informações úteis."
            else -> "${model.notes.size} anotações"
        }
        val notes = findViewById<LinearLayout>(R.id.notebookNotesRows)
        notes.removeAllViews()
        model.notes.forEach { note ->
            val row = row()
            row.addView(label(note.note.orEmpty()).apply { maxLines = 5; ellipsize = TextUtils.TruncateAt.END })
            row.addView(action("Abrir / editar") { model.openNote(note) })
            row.addView(action("Excluir anotação") {
                confirmDelete("Excluir esta anotação?") { model.deleteNote(note) }
            })
            notes.addView(row)
        }
        findViewById<TextView>(R.id.notebookChecklistState).text = when {
            model.checklistLoading -> "Carregando checklist…"
            model.checklistError != null -> model.checklistError
            model.checklistReady && !model.checklistSupported -> "O checklist ainda não está disponível. Suas anotações continuam acessíveis."
            model.items.isEmpty() -> "Crie itens para organizar o que precisa levar ou fazer."
            else -> "${model.items.count { it.completed }} de ${model.items.size} itens concluídos"
        }
        val items = findViewById<LinearLayout>(R.id.notebookChecklistRows)
        items.removeAllViews()
        model.items.forEach { item ->
            val row = row()
            row.addView(MaterialCheckBox(this).apply {
                text = item.label
                isChecked = item.completed
                minHeight = dp(48)
                setTextColor(getColor(R.color.vi_ink))
                isEnabled = !model.busy && !model.editorVisible
                setOnCheckedChangeListener { _, checked ->
                    if (checked != item.completed && isOwner()) model.toggle(item)
                }
            })
            row.addView(action("Editar item") { model.openItem(item) })
            row.addView(action("Excluir item") { confirmDelete("Excluir este item?") { model.deleteItem(item) } })
            items.addView(row)
        }
    }

    private fun label(text: String) = TextView(this).apply { this.text = text; textSize = 15f; setTextColor(getColor(R.color.vi_ink)); setPadding(0, dp(16), 0, 0) }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(8)) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun action(text: String, onClick: () -> Unit) = MaterialButton(this, null, android.R.attr.borderlessButtonStyle).apply {
        this.text = text
        minHeight = dp(48)
        isEnabled = !model.busy && !model.editorVisible
        setTextColor(getColor(R.color.vi_primary))
        setOnClickListener { if (isOwner()) onClick() else finish() }
    }

    private fun confirmDelete(title: String, action: () -> Unit) {
        MaterialAlertDialogBuilder(this).setTitle(title).setMessage("Esta ação não pode ser desfeita.")
            .setNegativeButton("Manter", null).setPositiveButton("Excluir") { _, _ -> if (isOwner()) action() else finish() }.show()
    }

    private fun discardEditor(action: () -> Unit) {
        if (model.busy) return
        if (!model.dirty) { action(); return }
        MaterialAlertDialogBuilder(this).setTitle("Descartar o texto não salvo?")
            .setNegativeButton("Continuar editando", null).setPositiveButton("Descartar") { _, _ -> action() }.show()
    }

    private fun closePage() {
        if (!::model.isInitialized) { finish(); return }
        discardEditor { finish() }
    }

    companion object {
        const val TRIP_ID = "notebook_trip_id"
        const val LOCAL_ID = "notebook_local_id"
        const val CONTEXT_NAME = "notebook_context_name"
    }
}
