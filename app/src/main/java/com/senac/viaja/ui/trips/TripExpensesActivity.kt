package com.senac.viaja.ui.trips

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.model.PrivateReceipt
import com.senac.viaja.data.model.ReceiptRules
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.main.setPageContent
import java.util.Calendar
import java.util.Locale

class TripExpensesActivity : AppCompatActivity() {
    private lateinit var model: TripExpensesViewModel
    private var boundVersion = -1
    private val receiptPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val expense = if (::model.isInitialized) model.receiptTarget else null
        if (uri != null && expense != null && isOwner()) model.uploadReceipt(applicationContext.contentResolver, uri, expense)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        val owner = AuthRepository(this).getUser()?.id ?: run { finish(); return }
        val tripId = intent.getLongExtra(TRIP_ID, -1L).takeIf { it > 0 } ?: run { finish(); return }
        setPageContent(R.layout.activity_trip_expenses, "Gastos e orçamento", null) { closePage() }
        model = ViewModelProvider(this)[TripExpensesViewModel::class.java]
        if (model.ownerId == 0L) {
            model.tripId = tripId
            model.ownerId = owner
            if (savedInstanceState?.getLong("draft_owner") == owner) restore(savedInstanceState)
        }
        findViewById<EditText>(R.id.expenseDescription).doAfterTextChanged { model.description = it.toString() }
        findViewById<EditText>(R.id.expenseAmount).doAfterTextChanged { model.amount = it.toString() }
        findViewById<EditText>(R.id.expenseDate).apply {
            doAfterTextChanged { model.date = it.toString() }
            setOnClickListener { pickDate() }
        }
        model.changed.observe(this) { render() }
        findViewById<View>(R.id.expensesAdd).setOnClickListener { if (isOwner()) model.open() else finish() }
        findViewById<View>(R.id.expensesSave).setOnClickListener { if (isOwner()) model.save() else finish() }
        findViewById<View>(R.id.expensesCancel).setOnClickListener { discard { model.cancel() } }
        findViewById<View>(R.id.expensesRefresh).setOnClickListener { if (isOwner()) model.refresh() else finish() }
        findViewById<View>(R.id.expensesFilter).setOnClickListener {
            val keys = listOf<String?>(null) + ExpenseRules.categories.keys
            val labels = listOf("Todas as categorias") + ExpenseRules.categories.values
            MaterialAlertDialogBuilder(this).setTitle("Filtrar despesas").setItems(labels.toTypedArray()) { _, index -> model.filter(keys[index]) }
                .setNegativeButton("Fechar", null).show()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closePage()
        })
        if (!model.ready && !model.loading) model.refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::model.isInitialized && !isOwner()) finish()
    }

    private fun isOwner() = AuthRepository(this).getUser()?.id == model.ownerId

    override fun onSaveInstanceState(outState: Bundle) {
        if (::model.isInitialized) {
            outState.putLong("draft_owner", model.ownerId)
            outState.putBoolean("editor_visible", model.editorVisible)
            outState.putString("editor_id", model.editorId)
            model.expenseVersion?.let { outState.putLong("expense_version", it) }
            outState.putStringArrayList("form", ArrayList(model.currentForm))
            outState.putStringArrayList("original_form", ArrayList(model.originalForm))
            outState.putString("category_filter", model.filterCategory)
            model.receiptTarget?.let { outState.putString("receipt_target", com.google.gson.Gson().toJson(it)) }
        }
        super.onSaveInstanceState(outState)
    }

    private fun restore(state: Bundle) {
        val fields = state.getStringArrayList("form") ?: return
        if (fields.size != 5) return
        model.description = fields[0]
        model.amount = fields[1]
        model.date = fields[2]
        model.category = fields[3]
        model.reimbursement = fields[4]
        model.originalForm = state.getStringArrayList("original_form").orEmpty()
        model.editorId = state.getString("editor_id").orEmpty()
        model.expenseVersion = if (state.containsKey("expense_version")) state.getLong("expense_version") else null
        model.editorVisible = state.getBoolean("editor_visible")
        model.filterCategory = state.getString("category_filter")
        runCatching {
            model.receiptTarget = com.google.gson.Gson().fromJson(state.getString("receipt_target"), com.senac.viaja.data.model.TripExpense::class.java)
        }
    }

    private fun render() {
        if (isFinishing || isDestroyed) return
        if (!isOwner()) { finish(); return }
        findViewById<TextView>(R.id.expensesSubtitle).text = model.tripName
        findViewById<TextView>(R.id.expensesStatus).text = model.mutationError ?: model.loadError ?: when {
            model.busy -> model.busyMessage
            model.loading -> "Atualizando gastos…"
            model.ready && !model.supported -> "O registro de gastos ainda não está disponível. Seu orçamento continua no painel da viagem."
            else -> model.notice.orEmpty()
        }
        val report = model.report
        findViewById<View>(R.id.expensesSummary).isVisible = report != null
        if (report != null) {
            findViewById<TextView>(R.id.expensesTotal).text = "Gasto total · ${ExpenseRules.currency(report.totalCents)}"
            findViewById<TextView>(R.id.expensesBudget).text = report.budgetCents?.let { "Orçamento · ${ExpenseRules.currency(it)}" } ?: "Orçamento não definido"
            findViewById<TextView>(R.id.expensesBalance).apply {
                text = ExpenseRules.balanceLabel(report.remainingCents)
                setTextColor(getColor(if ((report.remainingCents ?: 0) < 0) R.color.vi_warning else R.color.vi_ink))
            }
            findViewById<TextView>(R.id.expensesCategories).text = report.categoryTotals.entries.joinToString("\n") {
                "${ExpenseRules.categories[it.key] ?: "Outros"} · ${ExpenseRules.currency(it.value)}"
            }
            findViewById<TextView>(R.id.expensesRefunds).apply {
                isVisible = report.reimbursableCents > 0
                text = "Reembolsos informados\nRecebidos: ${ExpenseRules.currency(report.reimbursedCents)}\nPendentes: ${ExpenseRules.currency(report.reimbursableCents - report.reimbursedCents)}"
            }
        }
        findViewById<View>(R.id.expensesEditor).isVisible = model.editorVisible
        if (model.editorVisible && boundVersion != model.editorBinding) bindEditor()
        val editable = !model.busy && model.supported
        listOf(R.id.expenseDescription, R.id.expenseAmount, R.id.expenseDate, R.id.expenseCategory, R.id.expenseReimbursement, R.id.expensesSave).forEach {
            findViewById<View>(it).isEnabled = editable
        }
        findViewById<View>(R.id.expensesCancel).isEnabled = !model.busy
        findViewById<View>(R.id.expensesRefresh).isEnabled = !model.busy && !model.loading
        findViewById<View>(R.id.expensesAdd).isEnabled = editable && !model.editorVisible && !model.loading
        findViewById<TextView>(R.id.expensesFilter).text = ExpenseRules.categories[model.filterCategory] ?: "Todas as categorias"
        findViewById<View>(R.id.expensesFilter).isEnabled = report != null && !model.busy
        val rows = findViewById<LinearLayout>(R.id.expensesRows)
        rows.removeAllViews()
        val filtered = report?.expenses.orEmpty().filter { model.filterCategory == null || it.category == model.filterCategory }
        findViewById<TextView>(R.id.expensesListStatus).text = when {
            report == null -> ""
            filtered.isEmpty() && model.filterCategory != null -> "Nenhuma despesa nesta categoria."
            report.expenses.isEmpty() -> "Nenhuma despesa registrada."
            else -> "${filtered.size} despesas na lista"
        }
        filtered.forEach { expense ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(16), dp(20), dp(16))
                setBackgroundResource(R.drawable.vi_surface)
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) }
            }
            row.addView(label(expense.description, 17f))
            row.addView(label(ExpenseRules.currency(expense.amountCents), 20f))
            row.addView(label("${TripFormRules.displayDate(expense.expenseDate)} · ${ExpenseRules.categories[expense.category] ?: "Outros"}"))
            row.addView(label(ExpenseRules.reimbursements[expense.reimbursement] ?: "Não reembolsável"))
            row.addView(action(if (expense.hasReceipt) "Comprovante anexado" else "Anexar comprovante") {
                if (!expense.hasReceipt) pickReceipt(expense)
                else MaterialAlertDialogBuilder(this).setTitle("Comprovante privado")
                    .setItems(arrayOf("Abrir comprovante", "Substituir arquivo", "Remover comprovante")) { _, index ->
                        when (index) {
                            0 -> model.openReceipt(expense)
                            1 -> pickReceipt(expense)
                            2 -> MaterialAlertDialogBuilder(this).setTitle("Remover este comprovante?")
                                .setMessage("A despesa continuará registrada.").setNegativeButton("Manter", null)
                                .setPositiveButton("Remover") { _, _ -> if (isOwner()) model.removeReceipt(expense) else finish() }.show()
                        }
                    }.setNegativeButton("Fechar", null).show()
            })
            row.addView(action("Editar despesa") { model.open(expense) })
            row.addView(action("Excluir despesa") {
                MaterialAlertDialogBuilder(this).setTitle("Excluir ${expense.description}?")
                    .setMessage("O total de gastos será recalculado. Esta ação não pode ser desfeita.")
                    .setNegativeButton("Manter", null).setPositiveButton("Excluir") { _, _ -> if (isOwner()) model.delete(expense) else finish() }.show()
            })
            rows.addView(row)
        }
        model.receiptToOpen?.let { receipt ->
            model.receiptToOpen = null
            openPrivateReceipt(receipt)
        }
    }

    private fun bindEditor() {
        boundVersion = model.editorBinding
        findViewById<TextView>(R.id.expensesEditorTitle).text = if (model.expenseVersion == null) "Registrar despesa" else "Editar despesa"
        findViewById<EditText>(R.id.expenseDescription).setText(model.description)
        findViewById<EditText>(R.id.expenseAmount).setText(model.amount)
        findViewById<EditText>(R.id.expenseDate).setText(model.date)
        fun select(id: Int, options: Map<String, String>, value: String, update: (String) -> Unit) {
            val keys = options.keys.toList()
            findViewById<Spinner>(id).apply {
                adapter = ArrayAdapter(this@TripExpensesActivity, android.R.layout.simple_spinner_dropdown_item, options.values.toList())
                setSelection(keys.indexOf(value).coerceAtLeast(0))
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { update(keys[position]) }
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
            }
        }
        select(R.id.expenseCategory, ExpenseRules.categories, model.category) { model.category = it }
        select(R.id.expenseReimbursement, ExpenseRules.reimbursements, model.reimbursement) { model.reimbursement = it }
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        val parts = model.date.split('/')
        if (parts.size == 3) runCatching { calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt()) }
        DatePickerDialog(this, { _, year, month, day ->
            findViewById<EditText>(R.id.expenseDate).setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickReceipt(expense: com.senac.viaja.data.model.TripExpense) {
        model.receiptTarget = expense
        receiptPicker.launch(arrayOf("application/pdf", "image/png", "image/jpeg"))
    }

    private fun openPrivateReceipt(receipt: PrivateReceipt) {
        val directory = java.io.File(cacheDir, "expense_receipts")
        Thread {
            runCatching {
                check(directory.exists() || directory.mkdirs())
                directory.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 24 * 60 * 60 * 1000L }?.forEach { it.delete() }
                val file = java.io.File(directory, "${java.util.UUID.randomUUID()}.${ReceiptRules.extension(receipt.type)}")
                file.outputStream().use { it.write(receipt.bytes) }
                file
            }.onSuccess { file ->
                runOnUiThread {
                    if (!isFinishing && !isDestroyed && isOwner()) {
                        val uri = FileProvider.getUriForFile(this, "$packageName.receipts", file)
                        runCatching {
                            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, receipt.type)
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                        }.onFailure { model.receiptFailure("Nenhum aplicativo disponível para abrir este comprovante.") }
                    }
                }
            }.onFailure { runOnUiThread { if (!isFinishing && !isDestroyed) model.receiptFailure("Não foi possível preparar o comprovante para leitura.") } }
        }.start()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun label(text: String, size: Float = 14f) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(getColor(R.color.vi_ink))
        setPadding(0, dp(6), 0, dp(6))
    }
    private fun action(text: String, action: () -> Unit) = MaterialButton(this, null, android.R.attr.borderlessButtonStyle).apply {
        this.text = text
        minHeight = dp(48)
        isEnabled = !model.busy && !model.editorVisible && model.supported
        setTextColor(getColor(R.color.vi_primary))
        setOnClickListener { if (isOwner()) action() else finish() }
    }
    private fun discard(action: () -> Unit) {
        if (model.busy) return
        if (!model.dirty) { action(); return }
        MaterialAlertDialogBuilder(this).setTitle("Descartar os dados não salvos?")
            .setNegativeButton("Continuar editando", null).setPositiveButton("Descartar") { _, _ -> action() }.show()
    }
    private fun closePage() { if (!::model.isInitialized) finish() else discard { finish() } }

    companion object { const val TRIP_ID = "expenses_trip_id" }
}
