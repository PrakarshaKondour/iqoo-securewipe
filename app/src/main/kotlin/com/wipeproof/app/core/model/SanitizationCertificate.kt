package com.wipeproof.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SanitizationCertificate(
    val version: Int = 1,
    val caseId: String,
    val deviceManufacturer: String,
    val deviceModel: String,
    val deviceSerial: String?,
    val deviceAssetId: String?,
    val deviceFingerprint: String,
    val storageDescriptors: List<StorageDescriptorPayload>,
    val methodName: String,
    val methodClassification: String,
    val nistReference: String?,
    val sanitizationStartedAt: Long?,
    val sanitizationCompletedAt: Long?,
    val verificationOutcome: String,
    val evidenceHashes: List<EvidenceHashPayload>,
    val eventChainHash: String,
    val issuedAt: Long,
    val issuerDeviceFingerprint: String,
    val publicKeyPem: String,
    val isDemoMode: Boolean
) {
    companion object {
        const val DEMO_MARKER = "WIPEPROOF-DEMO-NOT-FOR-COMPLIANCE"
    }
}

@Serializable
data class StorageDescriptorPayload(
    val label: String,
    val type: String,
    val totalBytes: Long?,
    val isRemovable: Boolean,
    val isEncrypted: Boolean
)

@Serializable
data class EvidenceHashPayload(
    val label: String,
    val sha256: String,
    val description: String
)

@Serializable
data class SignedCertificate(
    val certificate: SanitizationCertificate,
    val signatureBase64: String
)

@Serializable
enum class VerificationOutcome {
    SANITIZED_VERIFIED,
    SANITIZED_UNVERIFIED,
    PARTIAL,
    FAILED,
    DEMO
}
