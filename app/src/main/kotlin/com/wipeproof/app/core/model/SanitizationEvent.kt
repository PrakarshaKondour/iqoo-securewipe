package com.wipeproof.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SanitizationEvent(
    val id: String,
    val caseId: String,
    val type: EventType,
    val timestamp: Long,
    val actorId: String,
    val description: String,
    val payload: String?,
    val hash: String,
    val previousHash: String?
)

@Serializable
enum class EventType {
    CASE_CREATED,
    DEVICE_IDENTIFIED,
    ASSESSMENT_COMPLETED,
    SANITIZATION_STARTED,
    SANITIZATION_PROGRESS,
    SANITIZATION_COMPLETED,
    SANITIZATION_FAILED,
    SANITIZATION_INTERRUPTED,
    VERIFICATION_STARTED,
    VERIFICATION_COMPLETED,
    VERIFICATION_FAILED,
    CERTIFICATE_GENERATED,
    CERTIFICATE_VERIFIED,
    HANDOVER_COMPLETED
}
