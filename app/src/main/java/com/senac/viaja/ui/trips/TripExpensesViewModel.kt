package com.senac.viaja.ui.trips

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.ViewModel
import com.senac.viaja.data.model.*
import com.senac.viaja.data.repository.ExpenseRepository
import com.senac.viaja.data.repository.TripRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TripExpensesViewModel : ViewModel() {
    val changed = MutableLiveData(0)
    var tripId = 0L
    var ownerId = 0L
    var tripName = ""
    var loading = false
    var busy = false
    var ready = false
    var supported = false
    var report: ExpenseReport? = null
    var loadError: String? = null
    var mutationError: String? = null
    var notice: String? = null
    var busyMessage = "Salvando alteração…"
    var receiptTarget: TripExpense? = null
    var receiptToOpen: PrivateReceipt? = null
    var filterCategory: String? = null
    var editorVisible = false
    var editorId = ""
    var expenseVersion: Long? = null
    var editorBinding = 0
    var description = ""
    var amount = ""
    var date = ""
    var category = "other"
    var reimbursement = "none"
    var originalForm = emptyList<String>()
    val currentForm get() = listOf(description, amount, date, category, reimbursement)
    val dirty get() = editorVisible && currentForm != originalForm
    private val repository = ExpenseRepository()

    private fun notifyChanged() { changed.value = (changed.value ?: 0) + 1 }

    fun refresh() {
        if (busy || loading) return
        loading = true
        loadError = null
        notifyChanged()
        TripRepository().getById(tripId, { tripName = it.name; notifyChanged() }, {})
        repository.report(tripId, { loaded, available ->
            report = loaded
            supported = available
            ready = true
            loading = false
            notifyChanged()
        }, { loading = false; loadError = it; notifyChanged() })
    }

    fun open(expense: TripExpense? = null) {
        if (busy || !supported) return
        editorId = expense?.id ?: UUID.randomUUID().toString()
        expenseVersion = expense?.version
        description = expense?.description.orEmpty()
        amount = TripFormRules.budgetInput(expense?.amountCents)
        date = expense?.expenseDate?.let { TripFormRules.displayDate(it) }
            ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        category = expense?.category ?: "other"
        reimbursement = expense?.reimbursement ?: "none"
        originalForm = currentForm
        editorVisible = true
        editorBinding++
        mutationError = null
        notice = null
        notifyChanged()
    }

    fun cancel() { if (!busy) { editorVisible = false; mutationError = null; notifyChanged() } }

    fun save() {
        if (busy || !supported || !editorVisible) return
        val values = runCatching { ExpenseRules.validate(description, amount, date, category, reimbursement) }
            .getOrElse { mutationError = it.message; notifyChanged(); return }
        busyMessage = "Salvando alteração…"
        busy = true
        mutationError = null
        notifyChanged()
        repository.save(tripId, editorId, ExpenseWrite(values.description, values.amountCents, values.date, values.category,
            values.reimbursement, expenseVersion), {
            busy = false
            editorVisible = false
            notice = "Despesa salva."
            notifyChanged()
            refresh()
        }, { busy = false; mutationError = it; notifyChanged() })
    }

    fun delete(expense: TripExpense) {
        if (busy || editorVisible || !supported) return
        busyMessage = "Salvando alteração…"
        busy = true
        mutationError = null
        notifyChanged()
        repository.delete(tripId, expense, {
            busy = false
            notice = "Despesa excluída."
            notifyChanged()
            refresh()
        }, { busy = false; mutationError = it; notifyChanged() })
    }

    fun uploadReceipt(resolver: android.content.ContentResolver, uri: android.net.Uri, expense: TripExpense) {
        if (busy || editorVisible || !supported) return
        busy = true
        busyMessage = "Anexando comprovante…"
        mutationError = null
        notifyChanged()
        viewModelScope.launch {
            val prepared = runCatching {
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { ReceiptRules.read(it) }
                        ?: throw IllegalArgumentException("Não foi possível ler o arquivo selecionado.")
                }
            }.getOrElse { receiptFailure(it.message ?: "Não foi possível ler o comprovante."); return@launch }
            repository.uploadReceipt(tripId, expense, prepared, {
                busy = false
                notice = "Comprovante anexado."
                notifyChanged()
                refresh()
            }, ::receiptFailure)
        }
    }

    fun openReceipt(expense: TripExpense) {
        if (busy || editorVisible || !supported) return
        busy = true
        busyMessage = "Carregando comprovante…"
        mutationError = null
        notifyChanged()
        repository.receipt(tripId, expense, {
            busy = false
            receiptToOpen = it
            notifyChanged()
        }, ::receiptFailure)
    }

    fun removeReceipt(expense: TripExpense) {
        if (busy || editorVisible || !supported) return
        busy = true
        busyMessage = "Removendo comprovante…"
        mutationError = null
        notifyChanged()
        repository.removeReceipt(tripId, expense, {
            busy = false
            notice = "Comprovante removido."
            notifyChanged()
            refresh()
        }, ::receiptFailure)
    }

    fun receiptFailure(message: String) { busy = false; mutationError = message; notifyChanged() }

    fun filter(category: String?) { filterCategory = category; notifyChanged() }
}
