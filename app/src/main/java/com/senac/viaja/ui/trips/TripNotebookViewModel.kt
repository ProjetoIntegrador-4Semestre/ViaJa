package com.senac.viaja.ui.trips

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.senac.viaja.data.model.*
import com.senac.viaja.data.repository.NotebookRepository
import com.senac.viaja.data.repository.TripRepository
import java.util.UUID

class TripNotebookViewModel : ViewModel() {
    val changed = MutableLiveData(0)
    var tripId = 0L
    var ownerId = 0L
    var localId: Long? = null
    var contextName = ""
    var tripName = ""
    var notes = emptyList<NotebookNote>()
    var items = emptyList<ChecklistItem>()
    var notesLoading = false
    var checklistLoading = false
    var notesError: String? = null
    var checklistError: String? = null
    var checklistSupported = false
    var checklistReady = false
    var notesReady = false
    var busy = false
    var mutationError: String? = null
    var editorVisible = false
    var editorChecklist = false
    var editorId: String? = null
    var editorRequestId = UUID.randomUUID().toString()
    var editorText = ""
    var editorOriginal = ""
    var editorCompleted = false
    var editorVersion = 0
    var notice: String? = null
    val dirty get() = editorVisible && editorText != editorOriginal
    private val repository = NotebookRepository()

    private fun notifyChanged() { changed.value = (changed.value ?: 0) + 1 }

    fun load() {
        if (busy) return
        if (!notesLoading) {
            notesLoading = true
            notesError = null
            TripRepository().getById(tripId, { tripName = it.name; notifyChanged() }, {})
            repository.notes(tripId, localId, {
                notes = it
                notesLoading = false
                notesReady = true
                notifyChanged()
            }, { notesLoading = false; notesError = it; notifyChanged() })
        }
        if (!checklistLoading) {
            checklistLoading = true
            checklistError = null
            repository.checklist(tripId, { loaded, supported ->
                items = loaded
                checklistSupported = supported
                checklistReady = true
                checklistLoading = false
                notifyChanged()
            }, { checklistLoading = false; checklistError = it; notifyChanged() })
        }
        notifyChanged()
    }

    fun openNote(note: NotebookNote? = null) {
        if (busy) return
        editorChecklist = false
        editorId = note?.id?.toString()
        editorText = note?.note.orEmpty()
        openEditor()
    }

    fun openItem(item: ChecklistItem? = null) {
        if (busy || !checklistSupported) return
        editorChecklist = true
        editorId = item?.id ?: UUID.randomUUID().toString()
        editorText = item?.label.orEmpty()
        editorCompleted = item?.completed ?: false
        openEditor()
    }

    private fun openEditor() {
        editorVisible = true
        editorOriginal = editorText
        editorRequestId = UUID.randomUUID().toString()
        editorVersion++
        mutationError = null
        notice = null
        notifyChanged()
    }

    fun cancelEditor() {
        if (busy) return
        editorVisible = false
        mutationError = null
        notifyChanged()
    }

    fun saveEditor() {
        if (busy || !editorVisible) return
        val value = runCatching { NotebookRules.text(editorText, editorChecklist) }.getOrElse {
            mutationError = it.message
            notifyChanged()
            return
        }
        busy = true
        mutationError = null
        notifyChanged()
        val success = {
            busy = false
            editorVisible = false
            notice = "Salvo."
            notifyChanged()
            load()
        }
        if (editorChecklist) repository.saveItem(tripId, editorId ?: return, value, editorCompleted, success, ::fail)
        else repository.saveNote(tripId, localId, editorId?.toLongOrNull(), value, editorRequestId, success, ::fail)
    }

    private fun fail(message: String) { busy = false; mutationError = message; notifyChanged() }

    fun toggle(item: ChecklistItem) {
        if (busy || !checklistSupported || editorVisible) return
        busy = true
        mutationError = null
        notifyChanged()
        repository.saveItem(tripId, item.id, item.label, !item.completed, { busy = false; load() }, ::fail)
    }

    fun deleteNote(note: NotebookNote) {
        if (busy) return
        busy = true
        mutationError = null
        notifyChanged()
        repository.deleteNote(tripId, localId, note.id, { busy = false; load() }, ::fail)
    }

    fun deleteItem(item: ChecklistItem) {
        if (busy || !checklistSupported) return
        busy = true
        mutationError = null
        notifyChanged()
        repository.deleteItem(tripId, item.id, { busy = false; load() }, ::fail)
    }
}
