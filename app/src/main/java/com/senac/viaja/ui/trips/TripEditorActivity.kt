package com.senac.viaja.ui.trips

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.main.setPageContent
import java.util.Calendar
import java.util.Locale

class TripEditorActivity : AppCompatActivity() {
    private lateinit var model: TripEditorViewModel
    private var bound = false
    private var restored: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        val user = AuthRepository(this).getUser() ?: run { finish(); return }
        val id = intent.getLongExtra(TRIP_ID, -1).takeIf { it > 0 }
        setPageContent(R.layout.activity_trip_editor, if (id == null) "Nova viagem" else "Editar viagem", null) { closeEditor() }
        model = ViewModelProvider(this)[TripEditorViewModel::class.java]
        restored = savedInstanceState?.takeIf { !model.ready && it.getBoolean("form_ready") }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closeEditor()
        })
        model.changed.observe(this) { render() }
        model.initialize(id, user.id, user.defaultMode)
        findViewById<View>(R.id.tripEditorRetry).setOnClickListener { model.load() }
        findViewById<View>(R.id.tripEditorSave).setOnClickListener {
            if (AuthRepository(this).getUser()?.id != model.ownerId) { finish(); return@setOnClickListener }
            model.save()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::model.isInitialized && AuthRepository(this).getUser()?.id != model.ownerId) finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("form_ready", model.ready)
        outState.putString("name", model.name)
        outState.putString("start", model.start)
        outState.putString("end", model.end)
        outState.putString("budget", model.budget)
        outState.putString("mode", model.mode)
        model.cityId?.let { outState.putLong("city", it) }
        super.onSaveInstanceState(outState)
    }

    private fun render() {
        if (isFinishing || isDestroyed) return
        model.saved?.let {
            model.saved = null
            setResult(RESULT_OK, Intent().putExtra(TRIP_ID, it.id.toLongOrNull() ?: -1L))
            finish()
            return
        }
        val status = findViewById<TextView>(R.id.tripEditorError)
        status.text = model.error ?: if (model.busy) "${if (model.ready) "Salvando" else "Carregando"}…" else ""
        status.isVisible = status.text.isNotBlank()
        findViewById<View>(R.id.tripEditorRetry).isVisible = !model.ready && !model.busy
        findViewById<View>(R.id.tripEditorFields).isVisible = model.ready
        if (model.ready && !bound) bindForm()
        listOf(R.id.tripEditorName, R.id.tripEditorStart, R.id.tripEditorEnd, R.id.tripEditorBudget, R.id.tripEditorMode).forEach {
            findViewById<View>(it).isEnabled = !model.busy
        }
        findViewById<View>(R.id.tripEditorCity).isEnabled = !model.busy && !model.hasStops
        findViewById<MaterialButton>(R.id.tripEditorSave).apply {
            isEnabled = model.ready && !model.busy
            text = if (model.busy) "Salvando…" else "Salvar viagem"
        }
    }

    private fun bindForm() {
        bound = true
        restored?.let {
            model.name = it.getString("name").orEmpty()
            model.start = it.getString("start").orEmpty()
            model.end = it.getString("end").orEmpty()
            model.budget = it.getString("budget").orEmpty()
            model.mode = it.getString("mode") ?: model.mode
            if (!model.hasStops && it.containsKey("city")) model.cityId = it.getLong("city")
            restored = null
        }
        fun field(id: Int, value: String, update: (String) -> Unit) {
            findViewById<EditText>(id).apply {
                setText(value)
                doAfterTextChanged { update(it.toString()) }
            }
        }
        field(R.id.tripEditorName, model.name) { model.name = it }
        field(R.id.tripEditorStart, model.start) { model.start = it }
        field(R.id.tripEditorEnd, model.end) { model.end = it }
        field(R.id.tripEditorBudget, model.budget) { model.budget = it }
        listOf(R.id.tripEditorStart, R.id.tripEditorEnd).forEach { id ->
            findViewById<EditText>(id).setOnClickListener { pickDate(it as EditText) }
        }
        findViewById<Spinner>(R.id.tripEditorCity).apply {
            adapter = ArrayAdapter(this@TripEditorActivity, android.R.layout.simple_spinner_dropdown_item,
                model.cities.map { listOfNotNull(it.name, it.state).joinToString(", ") })
            setSelection(model.cities.indexOfFirst { it.id == model.cityId }.coerceAtLeast(0))
            onItemSelectedListener = selection { position ->
                if (!model.hasStops) model.cityId = model.cities[position].id
            }
        }
        findViewById<Spinner>(R.id.tripEditorMode).apply {
            adapter = ArrayAdapter(this@TripEditorActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Turismo", "Trabalho"))
            setSelection(if (model.mode == "work") 1 else 0)
            onItemSelectedListener = selection { model.mode = if (it == 1) "work" else "tourism" }
        }
        findViewById<TextView>(R.id.tripEditorCityHint).text = if (model.hasStops)
            "Destino preservado: esta viagem já tem paradas no roteiro."
        else "Escolha a cidade principal desta viagem."
    }

    private fun selection(action: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { action(position) }
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private fun pickDate(field: EditText) {
        val today = Calendar.getInstance()
        val parts = field.text.toString().split('/')
        if (parts.size == 3) runCatching { today.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt()) }
        DatePickerDialog(this, { _, year, month, day ->
            field.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year))
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun closeEditor() {
        if (!::model.isInitialized) { finish(); return }
        if (!model.ready) { finish(); return }
        if (model.busy) {
            Toast.makeText(this, "Aguarde a operação terminar.", Toast.LENGTH_SHORT).show()
            return
        }
        val original = model.original
        val dirty = if (original == null) model.name.isNotBlank() || model.start.isNotBlank() || model.end.isNotBlank() || model.budget.isNotBlank()
        else model.name != original.name || model.start != TripFormRules.displayDate(original.startDate) ||
            model.end != TripFormRules.displayDate(original.endDate) || model.budget != TripFormRules.budgetInput(original.totalBudgetCents) ||
            model.mode != original.mode || model.cityId != original.cityId
        if (!dirty) { finish(); return }
        MaterialAlertDialogBuilder(this).setTitle("Descartar alterações?")
            .setMessage("As alterações deste formulário ainda não foram salvas.")
            .setNegativeButton("Continuar editando", null).setPositiveButton("Descartar") { _, _ -> finish() }.show()
    }

    companion object { const val TRIP_ID = "editor_trip_id" }
}
