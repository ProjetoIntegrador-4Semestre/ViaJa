package com.senac.viaja.data.repository

import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseRepository {
    fun report(tripId: Long, onSuccess: (ExpenseReport?, Boolean) -> Unit, onError: (String) -> Unit) {
        ApiClient.service.getExpenses(tripId).enqueue(object : Callback<ExpenseReport> {
            override fun onResponse(call: Call<ExpenseReport>, response: Response<ExpenseReport>) {
                val body = response.body()
                when {
                    response.isSuccessful && body != null -> onSuccess(body, true)
                    response.code() == 404 || response.code() == 405 -> onSuccess(null, false)
                    else -> onError(message(response.code()))
                }
            }
            override fun onFailure(call: Call<ExpenseReport>, error: Throwable) { onError("Não foi possível consultar os gastos. Tente novamente.") }
        })
    }

    fun save(tripId: Long, id: String, request: ExpenseWrite, onSuccess: (TripExpense) -> Unit, onError: (String) -> Unit) {
        ApiClient.service.saveExpense(tripId, id, request).enqueue(object : Callback<TripExpense> {
            override fun onResponse(call: Call<TripExpense>, response: Response<TripExpense>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body) else onError(message(response.code()))
            }
            override fun onFailure(call: Call<TripExpense>, error: Throwable) {
                onError("Não foi possível confirmar o salvamento. Seus dados foram mantidos; repetir este pedido não cria outra despesa.")
            }
        })
    }

    fun delete(tripId: Long, expense: TripExpense, onSuccess: () -> Unit, onError: (String) -> Unit) {
        ApiClient.service.deleteExpense(tripId, expense.id, expense.version).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) onSuccess() else onError(message(response.code()))
            }
            override fun onFailure(call: Call<Void>, error: Throwable) { onError("Não foi possível confirmar a exclusão. Atualize os gastos para conferir.") }
        })
    }

    fun uploadReceipt(tripId: Long, expense: TripExpense, receipt: PrivateReceipt, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val body = okhttp3.RequestBody.create(okhttp3.MediaType.parse(receipt.type), receipt.bytes)
        val file = okhttp3.MultipartBody.Part.createFormData("file", "comprovante.${ReceiptRules.extension(receipt.type)}", body)
        receiptMutation(ApiClient.service.uploadReceipt(tripId, expense.id, expense.version, file), onSuccess, onError)
    }

    fun removeReceipt(tripId: Long, expense: TripExpense, onSuccess: () -> Unit, onError: (String) -> Unit) =
        receiptMutation(ApiClient.service.removeReceipt(tripId, expense.id, expense.version), onSuccess, onError)

    private fun receiptMutation(call: Call<TripExpense>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<TripExpense> {
            override fun onResponse(call: Call<TripExpense>, response: Response<TripExpense>) {
                if (response.isSuccessful && response.body() != null) onSuccess() else onError(message(response.code()))
            }
            override fun onFailure(call: Call<TripExpense>, error: Throwable) { onError("Não foi possível confirmar a alteração do comprovante. Atualize os gastos para conferir.") }
        })
    }

    fun receipt(tripId: Long, expense: TripExpense, onSuccess: (PrivateReceipt) -> Unit, onError: (String) -> Unit) {
        ApiClient.service.getReceipt(tripId, expense.id).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                val body = response.body()
                if (!response.isSuccessful || body == null) { onError("O comprovante não está disponível. Atualize os dados para conferir."); return }
                runCatching { body.use { ReceiptRules.read(it.byteStream()) } }
                    .onSuccess(onSuccess).onFailure { onError(it.message ?: "Não foi possível abrir o comprovante.") }
            }
            override fun onFailure(call: Call<okhttp3.ResponseBody>, error: Throwable) { onError("Não foi possível baixar o comprovante. Tente novamente.") }
        })
    }

    private fun message(code: Int) = when (code) {
        401 -> "Sua sessão expirou. Entre novamente."
        409 -> "Esta despesa mudou ou foi removida em outra sessão. Atualize os dados e confira a versão atual antes de editar; seu formulário foi preservado."
        404, 405 -> "O registro de gastos está indisponível. Seus dados foram mantidos."
        413 -> "O comprovante deve ter até 5 MiB."
        400 -> "Os dados não foram aceitos. Revise valor, data e categoria."
        else -> "Não foi possível concluir ($code). Seus dados foram mantidos."
    }
}
