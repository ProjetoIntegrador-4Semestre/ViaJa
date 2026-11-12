package com.senac.viaja.data.remote

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Assert.*
import org.junit.Test

class ApiHeadersTest {
    @Test fun keepsMultipartBoundaryAndAuthentication() {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", "comprovante.pdf", RequestBody.create(MediaType.parse("application/pdf"), "%PDF-1.7"))
            .build()
        val request = apiRequestWithHeaders(Request.Builder().url("https://example.invalid/receipt")
            .put(body).header("Content-Type", "application/json").build(), "test-token")
        assertEquals(body.contentType().toString(), request.header("Content-Type"))
        assertTrue(request.header("Content-Type")!!.contains("boundary="))
        assertEquals("Bearer test-token", request.header("Authorization"))
    }

    @Test fun preservesBinaryAcceptWhileJsonRequestsKeepTheirContract() {
        val binary = apiRequestWithHeaders(Request.Builder().url("https://example.invalid/receipt").header("Accept", "*/*").build(), null)
        assertEquals("*/*", binary.header("Accept"))
        val json = apiRequestWithHeaders(Request.Builder().url("https://example.invalid/trips")
            .post(RequestBody.create(MediaType.parse("application/json"), "{}")).build(), null)
        assertEquals("application/json", json.header("Accept"))
        assertTrue(json.header("Content-Type")!!.startsWith("application/json"))
        assertNull(json.header("Authorization"))
    }
}
