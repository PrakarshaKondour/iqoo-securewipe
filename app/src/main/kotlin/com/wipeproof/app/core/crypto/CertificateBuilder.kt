package com.wipeproof.app.core.crypto

import com.wipeproof.app.core.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateBuilder @Inject constructor(
    private val cryptoEngine: CryptoEngine
) {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun buildAndSign(
        case_: SanitizationCase,
        events: List<SanitizationEvent>,
        verificationOutcome: VerificationOutcome,
        evidenceHashes: List<EvidenceHashPayload>
    ): SignedCertificate {
        val deviceInfo = case_.deviceInfo
        val method = case_.selectedMethod ?: SanitizationMethod.NOT_SUPPORTED

        val sanitizationStartedAt = events
            .filter { it.type == EventType.SANITIZATION_STARTED }
            .minOfOrNull { it.timestamp }
        val sanitizationCompletedAt = events
            .filter { it.type == EventType.SANITIZATION_COMPLETED }
            .maxOfOrNull { it.timestamp }

        val eventChainHash = computeChainHash(events)

        val certificate = SanitizationCertificate(
            version = 1,
            caseId = case_.caseId,
            deviceManufacturer = deviceInfo.manufacturer,
            deviceModel = deviceInfo.model,
            deviceSerial = deviceInfo.serialNumber,
            deviceAssetId = deviceInfo.assetId,
            deviceFingerprint = deviceInfo.fingerprint,
            storageDescriptors = deviceInfo.storageDescriptors.map { sd ->
                StorageDescriptorPayload(
                    label = sd.label,
                    type = sd.type.name,
                    totalBytes = sd.totalBytes,
                    isRemovable = sd.isRemovable,
                    isEncrypted = sd.isEncrypted
                )
            },
            methodName = method.displayName,
            methodClassification = method.classification.name,
            nistReference = method.nistReference,
            sanitizationStartedAt = sanitizationStartedAt,
            sanitizationCompletedAt = sanitizationCompletedAt,
            verificationOutcome = verificationOutcome.name,
            evidenceHashes = evidenceHashes,
            eventChainHash = eventChainHash,
            issuedAt = System.currentTimeMillis(),
            issuerDeviceFingerprint = deviceInfo.fingerprint,
            publicKeyPem = cryptoEngine.getPublicKeyPem(),
            isDemoMode = case_.isDemoMode
        )

        val canonicalPayload = json.encodeToString(certificate)
        val payloadBytes = canonicalPayload.toByteArray(Charsets.UTF_8)
        val signatureBase64 = cryptoEngine.sign(payloadBytes)

        return SignedCertificate(
            certificate = certificate,
            signatureBase64 = signatureBase64
        )
    }

    fun encodeToQrPayload(signedCert: SignedCertificate): String {
        val jsonStr = json.encodeToString(signedCert)
        val compressed = deflate(jsonStr.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(compressed)
    }

    fun decodeFromQrPayload(qrPayload: String): SignedCertificate {
        val compressed = Base64.getUrlDecoder().decode(qrPayload)
        val jsonStr = inflate(compressed)
        return json.decodeFromString(jsonStr)
    }

    private fun computeChainHash(events: List<SanitizationEvent>): String {
        if (events.isEmpty()) return cryptoEngine.sha256Hex("EMPTY_CHAIN")
        val sorted = events.sortedBy { it.timestamp }
        val concatenated = sorted.joinToString("|") { it.hash }
        return cryptoEngine.sha256Hex(concatenated)
    }

    private fun deflate(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        DeflaterOutputStream(bos, deflater).use { it.write(data) }
        return bos.toByteArray()
    }

    private fun inflate(data: ByteArray): String {
        val inflater = InflaterInputStream(data.inputStream())
        return inflater.readBytes().toString(Charsets.UTF_8)
    }
}
