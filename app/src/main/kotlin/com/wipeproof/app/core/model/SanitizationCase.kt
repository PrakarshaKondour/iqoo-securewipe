package com.wipeproof.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SanitizationCase(
    val id: String,
    val caseId: String, // e.g. WP-YYYYMMDD-XXXX
    val deviceInfo: DeviceInfo,
    val status: CaseStatus,
    val selectedMethod: SanitizationMethod?,
    val isDemoMode: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

enum class CaseStatus {
    IDENTIFIED,
    ASSESSED,
    SANITIZING,
    SANITIZED,
    VERIFIED,
    CERTIFIED,
    HANDED_OVER,
    FAILED
}
