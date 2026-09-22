package com.wipeproof.app.domain.usecase

import com.wipeproof.app.core.model.*
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.data.repository.CertificateRepository
import com.wipeproof.app.data.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

data class HandoverCheck(
    val title: String,
    val passed: Boolean,
    val detail: String
)

data class HandoverReadiness(
    val isReady: Boolean,
    val checks: List<HandoverCheck>
)

data class PassportData(
    val case_: SanitizationCase,
    val events: List<SanitizationEvent>,
    val signedCertificate: SignedCertificate?,
    val qrPayload: String?,
    val readiness: HandoverReadiness,
    val chainIntegrityValid: Boolean
)

@Singleton
class GetPassportUseCase @Inject constructor(
    private val caseRepository: CaseRepository,
    private val eventRepository: EventRepository,
    private val certificateRepository: CertificateRepository
) {

    suspend fun execute(caseId: String): PassportData? {
        val case_ = caseRepository.getByCaseId(caseId) ?: return null
        val events = eventRepository.getEventsForCase(caseId)
        val certPair = certificateRepository.getByCaseId(caseId)
        val chainIntegrity = eventRepository.verifyChainIntegrity(caseId)

        val hasDeviceIdentified = events.any { it.type == EventType.DEVICE_IDENTIFIED }
        val hasSanitizeComplete = events.any { it.type == EventType.SANITIZATION_COMPLETED }
        val hasCertIssued = certPair != null
        val noUnsupportedStorage = case_.deviceInfo.storageDescriptors.none {
            it.type == StorageType.UNKNOWN
        }

        val checks = listOf(
            HandoverCheck(
                title = "Device Identity Authenticated",
                passed = hasDeviceIdentified,
                detail = "Hardware identifiers and device cryptographic fingerprint verified."
            ),
            HandoverCheck(
                title = "Sanitization Procedure Executed",
                passed = hasSanitizeComplete,
                detail = "Target storage reached terminal sanitization state without unhandled exception."
            ),
            HandoverCheck(
                title = "Independent Verification Certified",
                passed = hasCertIssued,
                detail = "ECDSA P-256 certificate generated and tamper-sealed."
            ),
            HandoverCheck(
                title = "Storage Boundaries Assessed",
                passed = noUnsupportedStorage,
                detail = "All attached media accounted for with no unverified unknown volumes."
            ),
            HandoverCheck(
                title = "Audit Chain Cryptographic Integrity",
                passed = chainIntegrity,
                detail = "SHA-256 forward-linked event hash chain verified unbroken."
            )
        )

        val isReady = checks.all { it.passed }

        return PassportData(
            case_ = case_,
            events = events,
            signedCertificate = certPair?.first,
            qrPayload = certPair?.second,
            readiness = HandoverReadiness(isReady, checks),
            chainIntegrityValid = chainIntegrity
        )
    }

    suspend fun markHandoverComplete(caseId: String, actorId: String = "HANDOVER_OFFICER") {
        eventRepository.recordEvent(
            caseId = caseId,
            type = EventType.HANDOVER_COMPLETED,
            actorId = actorId,
            description = "Device custody transferred. Sanitization passport archived.",
            payload = "{\"status\":\"HANDED_OVER\"}"
        )
        caseRepository.getByCaseId(caseId)?.let { c ->
            caseRepository.updateStatus(c.id, CaseStatus.HANDED_OVER)
        }
    }
}
