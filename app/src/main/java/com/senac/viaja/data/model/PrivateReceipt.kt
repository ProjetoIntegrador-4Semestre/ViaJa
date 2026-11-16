package com.senac.viaja.data.model

import java.io.ByteArrayOutputStream
import java.io.InputStream

data class PrivateReceipt(val bytes: ByteArray, val type: String)

internal object ReceiptRules {
    const val MAX_BYTES = 5 * 1024 * 1024

    fun read(stream: InputStream): PrivateReceipt {
        val buffer = ByteArray(8192)
        val output = ByteArrayOutputStream()
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            require(output.size() + count <= MAX_BYTES) { "O comprovante deve ter até 5 MiB." }
            output.write(buffer, 0, count)
        }
        val bytes = output.toByteArray()
        return PrivateReceipt(bytes, type(bytes))
    }

    fun type(bytes: ByteArray): String = when {
        bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray(Charsets.US_ASCII)) -> "application/pdf"
        bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)) -> "image/png"
        bytes.size >= 3 && bytes[0] == 255.toByte() && bytes[1] == 216.toByte() && bytes[2] == 255.toByte() -> "image/jpeg"
        else -> throw IllegalArgumentException("Selecione um comprovante em PDF, PNG ou JPEG.")
    }

    fun extension(type: String) = when (type) { "application/pdf" -> "pdf"; "image/png" -> "png"; else -> "jpg" }
}
