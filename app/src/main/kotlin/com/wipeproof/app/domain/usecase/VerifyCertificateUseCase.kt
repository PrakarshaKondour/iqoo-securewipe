package com.wipeproof.app.domain.usecase

import com.wipeproof.app.core.crypto.CertificateVerifier
import com.wipeproof.app.core.model.EventType
import com.wipeproof.app.core.model.VerificationResult
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.data.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifyCertificateUseCase @Inject constructor(
    private val certificateVerifier: CertificateVerifier,
    private val caseRepository: CaseRepository,
    private val eventRepository: EventRepository
) {

    suspend fun execute(qrPayload: String, verifierActorId: String = "VERIFIER_DEVICE"): VerificationResult {
        val result = certificateVerifier.verifyQrPayload(qrPayload)

        result.caseId?.let { caseId ->
            val existing = caseRepository.getByCaseId(caseId)
            if (existing != null) {
                eventRepository.recordEvent(
                    caseId = caseId,
                    type = EventType.CERTIFICATE_VERIFIED,
                    actorId = verifierActorId,
                    description = "Independent 2nd-party certificate verification performed: ${result.status.name}",
                    payload = "{\"valid\":${result.signatureValid},\"status\":\"${result.status.name}\"}"
                )
            }
        }

        return result
    }
}
