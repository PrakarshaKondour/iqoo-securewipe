package com.wipeproof.app.officekit

import com.wipeproof.app.core.model.EvidenceHashPayload
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class OfficekitDevice(
    val name: String,
    val path: String,
    val size: String,
    val type: String,
    val model: String?,
    val removable: Boolean
)

@Serializable
data class OfficekitStatusResponse(
    val status: String,
    val version: String
)

@Serializable
data class OfficekitSanitizeResponse(
    val operationId: String,
    val status: String
)

@Serializable
data class OfficekitEvidence(
    val operationId: String,
    val method: String,
    val targetDevice: String,
    val startedAt: Long,
    val completedAt: Long,
    val passCount: Int,
    val evidenceHashes: List<EvidenceHashPayload>,
    val success: Boolean
)

@Singleton
class OfficekitBridgeClient @Inject constructor() {

    private val baseUrl = "http://127.0.0.1:8765"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$baseUrl/ping")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun listDevices(): List<OfficekitDevice> = withContext(Dispatchers.IO) {
        try {
            client.get("$baseUrl/devices").body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun startSanitization(devicePath: String, method: String): String? = withContext(Dispatchers.IO) {
        try {
            val response: OfficekitSanitizeResponse = client.post("$baseUrl/sanitize") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("device" to devicePath, "method" to method))
            }.body()
            response.operationId
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEvidence(operationId: String): OfficekitEvidence? = withContext(Dispatchers.IO) {
        try {
            client.get("$baseUrl/evidence/$operationId").body()
        } catch (e: Exception) {
            null
        }
    }
}
