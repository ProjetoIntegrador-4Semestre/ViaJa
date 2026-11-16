package com.senac.viaja.data.repository

import android.content.Context
import android.util.AtomicFile
import com.google.gson.Gson
import com.senac.viaja.data.model.TripBundle
import java.io.File
import java.io.IOException

class OfflineRepository(context: Context, private val expectedOwner: Long? = AuthRepository(context).offlineOwner()) {
    private val app = context.applicationContext
    private val gson = Gson()

    private fun owner(): Long {
        val current = AuthRepository(app).offlineOwner()
        check(current != null && current == expectedOwner) { "Entre na conta para consultar suas viagens baixadas." }
        return current
    }

    private fun directory(owner: Long) = File(app.filesDir, "offline_trips/$owner")

    fun save(bundle: TripBundle) {
        val account = owner()
        require(bundle.schemaVersion == 1 && bundle.ownerId == account)
        val id = bundle.trip.id.toLong().also { require(it > 0) }
        val directory = directory(account)
        check(directory.isDirectory || directory.mkdirs()) { "Não foi possível preparar o armazenamento." }
        val file = AtomicFile(File(directory, "$id.json"))
        val out = file.startWrite()
        try {
            out.write(gson.toJson(bundle).toByteArray(Charsets.UTF_8))
            owner()
            file.finishWrite(out)
        } catch (error: Exception) {
            file.failWrite(out)
            throw error
        }
    }

    fun list(): List<TripBundle> {
        val account = owner()
        val files = directory(account).listFiles().orEmpty().mapNotNull {
            when {
                it.name.endsWith(".json") -> it
                it.name.endsWith(".json.bak") -> File(it.path.removeSuffix(".bak"))
                else -> null
            }
        }.distinctBy { it.path }
        val result = files.mapNotNull { file ->
            runCatching {
                AtomicFile(file).openRead().bufferedReader().use {
                    gson.fromJson(it, TripBundle::class.java)
                }.takeIf { it.schemaVersion == 1 && it.ownerId == account && it.trip.id.toLong() > 0 }
            }.getOrNull()
        }.sortedByDescending { it.downloadedAt }
        owner()
        return result
    }

    fun remove(id: String) {
        val account = owner()
        val trip = id.toLong().also { require(it > 0) }
        val file = File(directory(account), "$trip.json")
        AtomicFile(file).delete()
        if (listOf(file, File(file.path + ".bak"), File(file.path + ".new")).any { it.exists() }) {
            throw IOException("Não foi possível remover o download.")
        }
        owner()
    }
}
