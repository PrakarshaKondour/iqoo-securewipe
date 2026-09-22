package com.wipeproof.app.domain.usecase

import com.wipeproof.app.core.crypto.CertificateBuilder
import com.wipeproof.app.core.model.*
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.data.repository.CertificateRepository
import com.wipeproof.app.data.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildCertificateUseCase @Inject constructor(
    private val certificateBuilder: CertificateBuilder,
    private val collectEvidenceUseCase: CollectEvidenceUseCase,
    private val caseRepository: CaseRepository,
    private val eventRepository: EventRepository,
    private val certificateRepository: CertificateRepository
) {

    suspend fun execute(case_: SanitizationCase): Pair<SignedCertificate, String> {
        val events = eventRepository.getEventsForCase(case_.caseId)
        val evidenceHashes = collectEvidenceUseCase.execute(case_)

        val outcome = when {
            case_.isDemoMode -> VerificationOutcome.DEMO
            case_.status == CaseStatus.FAILED -> VerificationOutcome.FAILED
            evidenceHashes.isNotEmpty() -> VerificationOutcome.SANITIZED_VERIFIED
            else -> VerificationOutcome.SANITIZED_UNVERIFIED
        }

        // Record VERIFICATION_STARTED and COMPLETED
        eventRepository.recordEvent(
            caseId = case_.caseId,
            type = EventType.VERIFICATION_STARTED,
            actorId = case_.deviceInfo.fingerprint.take(12),
            description = "Independent technical evidence verification initiated.",
            payload = "{\"evidenceCount\":${evidenceHashes.size}}"
        )

        eventRepository.recordEvent(
            caseId = case_.caseId,
            type = EventType.VERIFICATION_COMPLETED,
            actorId = case_.deviceInfo.fingerprint.take(12),
            description = "Verification concluded with outcome: ${outcome.name}",
            payload = "{\"outcome\":\"${outcome.name}\"}"
        )

        val allEvents = eventRepository.getEventsForCase(case_.caseId)
        val signedCert = certificateBuilder.buildAndSign(case_, allEvents, outcome, evidenceHashes)
        val qrPayload = certificateBuilder.encodeToQrPayload(signedCert)

        certificateRepository.saveCertificate(case_.caseId, signedCert, qrPayload)
        caseRepository.updateStatus(case_.id, CaseStatus.CERTIFIED)

        eventRepository.recordEvent(
            caseId = case_.caseId,
            type = EventType.CERTIFICATE_GENERATED,
            actorId = case_.deviceInfo.fingerprint.take(12),
            description = "ECDSA P-256 signed sanitization certificate issued and tamper-sealed.",
            payload = "{\"certVersion\":${signedCert.certificate.version},\"pubKey\":${signedCert.certificate.publicKeyPem.take(32)}}"
        )

        return Pair(signedCert, qrPayload)
    }
}
