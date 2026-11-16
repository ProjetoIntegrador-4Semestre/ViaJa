package com.senac.viaja.data.repository

import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotebookRepository {
    private val service get() = ApiClient.service

    fun notes(tripId: Long, localId: Long?, onSuccess: (List<NotebookNote>) -> Unit, onError: (String) -> Unit) {
        if (localId != null) {
            receive(service.getTripMemos(tripId, localId), { items ->
                onSuccess(items.mapNotNull { memo -> memo.id?.let { NotebookNote(it, memo.note) } })
            }, onError)
        } else notesPage(tripId, 0, mutableListOf(), onSuccess, onError)
    }

    private fun notesPage(tripId: Long, page: Int, items: MutableList<NotebookNote>, onSuccess: (List<NotebookNote>) -> Unit, onError: (String) -> Unit) {
        receive(service.getTripNotes(tripId, page = page), { result ->
            items.addAll(result._embedded?.notes.orEmpty().mapNotNull { note -> note.id?.let { NotebookNote(it, note.note) } })
            if (page + 1 < (result.page?.totalPages ?: 1)) notesPage(tripId, page + 1, items, onSuccess, onError)
            else onSuccess(items)
        }, onError)
    }

    fun saveNote(tripId: Long, localId: Long?, id: Long?, text: String, requestId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val call = when {
            id != null && localId != null -> service.updateTripMemo(id, NoteWrite(text))
            id != null -> service.updateTripNote(tripId, id, NoteWrite(text))
            localId != null -> service.createTripMemo(MemoWrite(tripId, localId, text, requestId))
            else -> service.createTripNote(tripId, NoteWrite(text, requestId))
        }
        receive(call, { onSuccess() }, onError)
    }

    fun deleteNote(tripId: Long, localId: Long?, id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) =
        remove(if (localId == null) service.deleteTripNote(tripId, id) else service.deleteTripMemo(id), onSuccess, onError)

    fun checklist(tripId: Long, onSuccess: (List<ChecklistItem>, Boolean) -> Unit, onError: (String) -> Unit) {
        service.getChecklist(tripId).enqueue(object : Callback<List<ChecklistItem>> {
            override fun onResponse(call: Call<List<ChecklistItem>>, response: Response<List<ChecklistItem>>) {
                val body = response.body()
                when {
                    response.isSuccessful && body != null -> onSuccess(body, true)
                    response.code() == 404 || response.code() == 405 -> onSuccess(emptyList(), false)
                    else -> onError(error(response.code()))
                }
            }
            override fun onFailure(call: Call<List<ChecklistItem>>, failure: Throwable) { onError("Não foi possível consultar o checklist. Tente novamente.") }
        })
    }

    fun saveItem(tripId: Long, id: String, label: String, completed: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) =
        receive(service.saveChecklistItem(tripId, id, ChecklistWrite(label, completed)), { onSuccess() }, onError)

    fun deleteItem(tripId: Long, id: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        remove(service.deleteChecklistItem(tripId, id), onSuccess, onError)

    private fun error(code: Int) = when (code) {
        401 -> "Sua sessão expirou. Entre novamente."
        409 -> "Esta solicitação já foi processada com outro conteúdo. Recarregue e confira o que foi salvo antes de continuar."
        404 -> "O item ou a viagem não está mais disponível. Recarregue a página."
        else -> "Não foi possível concluir ($code). O texto foi preservado; recarregue para conferir o estado salvo."
    }

    private fun <T> receive(call: Call<T>, onSuccess: (T) -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body) else onError(error(response.code()))
            }
            override fun onFailure(call: Call<T>, failure: Throwable) {
                onError("Não foi possível confirmar a operação. Seu texto foi preservado; confira o estado salvo antes de repetir.")
            }
        })
    }

    private fun remove(call: Call<Void>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) onSuccess() else onError(error(response.code()))
            }
            override fun onFailure(call: Call<Void>, failure: Throwable) { onError("Não foi possível confirmar a exclusão. Recarregue para conferir.") }
        })
    }
}
