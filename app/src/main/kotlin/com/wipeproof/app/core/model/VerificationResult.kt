package com.wipeproof.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class VerificationResult(
    val status: VerificationStatus,
    val caseId: String?,
    val deviceFingerprint: String?,
    val method: String?,
    val issuedAt: Long?,
    val isDemoMode: Boolean,
    val signatureValid: Boolean,
    val chainIntegrityValid: Boolean,
    val failureReason: String?,
    val failedField: String?,
    val checkedAt: Long
)

@Serializable
enum class VerificationStatus {
    VERIFIED,
    INVALID,
    TAMPERED,
    DEMO
}
