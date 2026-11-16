package com.senac.viaja.ui.trips

import com.senac.viaja.data.model.ReceiptRules
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class ReceiptRulesTest {
    @Test fun detectsContentsInsteadOfTrustingFileName() {
        assertEquals("application/pdf", ReceiptRules.type("%PDF-1.7\n".toByteArray()))
        assertEquals("image/png", ReceiptRules.type(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)))
        assertEquals("image/jpeg", ReceiptRules.type(byteArrayOf(255.toByte(), 216.toByte(), 255.toByte(), 224.toByte())))
        assertTrue(runCatching { ReceiptRules.type("<html>file.pdf</html>".toByteArray()) }.isFailure)
    }

    @Test fun rejectsEmptyAndOversizedStreams() {
        assertTrue(runCatching { ReceiptRules.read(ByteArrayInputStream(byteArrayOf())) }.isFailure)
        assertTrue(runCatching { ReceiptRules.read(ByteArrayInputStream(ByteArray(ReceiptRules.MAX_BYTES + 1))) }.isFailure)
    }

    @Test fun readsBoundedReceiptWithoutChangingBytes() {
        val bytes = "%PDF-1.7\ncomprovante".toByteArray()
        val receipt = ReceiptRules.read(ByteArrayInputStream(bytes))
        assertArrayEquals(bytes, receipt.bytes)
        assertEquals("pdf", ReceiptRules.extension(receipt.type))
    }
}
