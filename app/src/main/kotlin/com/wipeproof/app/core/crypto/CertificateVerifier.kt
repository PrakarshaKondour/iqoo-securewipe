package com.wipeproof.app.core.crypto

import com.wipeproof.app.core.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateVerifier @Inject constructor(
    private val cryptoEngine: CryptoEngine,
    private val certificateBuilder: CertificateBuilder
) {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun verifyQrPayload(qrPayload: String): VerificationResult {
        val now = System.currentTimeMillis()

        val signedCert = try {
            certificateBuilder.decodeFromQrPayload(qrPayload)
        } catch (e: Exception) {
            return VerificationResult(
                status = VerificationStatus.INVALID,
                caseId = null,
                deviceFingerprint = null,
                method = null,
                issuedAt = null,
                isDemoMode = false,
                signatureValid = false,
                chainIntegrityValid = false,
                failureReason = "Certificate payload is malformed or corrupted: ${e.localizedMessage ?: e.message}",
                failedField = "payload",
                checkedAt = now
            )
        }

        return verifySignedCertificate(signedCert, now)
    }

    fun verifySignedCertificate(
        signedCert: SignedCertificate,
        checkedAt: Long = System.currentTimeMillis()
    ): VerificationResult {
        val cert = signedCert.certificate

        val canonicalPayload = try {
            json.encodeToString(cert)
        } catch (e: Exception) {
            return VerificationResult(
                status = VerificationStatus.INVALID,
                caseId = cert.caseId,
                deviceFingerprint = cert.deviceFingerprint,
                method = cert.methodName,
                issuedAt = cert.issuedAt,
                isDemoMode = cert.isDemoMode,
                signatureValid = false,
                chainIntegrityValid = false,
                failureReason = "Failed to reconstruct canonical certificate form: ${e.message}",
                failedField = "certificate",
                checkedAt = checkedAt
            )
        }

        val payloadBytes = canonicalPayload.toByteArray(Charsets.UTF_8)
        val signatureValid = try {
            cryptoEngine.verify(payloadBytes, signedCert.signatureBase64, cert.publicKeyPem)
        } catch (e: Exception) {
            false
        }

        if (!signatureValid) {
            return VerificationResult(
                status = VerificationStatus.TAMPERED,
                caseId = cert.caseId,
                deviceFingerprint = cert.deviceFingerprint,
                method = cert.methodName,
                issuedAt = cert.issuedAt,
                isDemoMode = cert.isDemoMode,
                signatureValid = false,
                chainIntegrityValid = false,
                failureReason = "Cryptographic signature verification FAILED. Certificate content was altered or signed with an untrusted key.",
                failedField = "signature",
                checkedAt = checkedAt
            )
        }

        val status = if (cert.isDemoMode) VerificationStatus.DEMO else VerificationStatus.VERIFIED

        return VerificationResult(
            status = status,
            caseId = cert.caseId,
            deviceFingerprint = cert.deviceFingerprint,
            method = cert.methodName,
            issuedAt = cert.issuedAt,
            isDemoMode = cert.isDemoMode,
            signatureValid = true,
            chainIntegrityValid = true,
            failureReason = null,
            failedField = null,
            checkedAt = checkedAt
        )
    }
}
