package com.wipeproof.app.domain.usecase

import com.wipeproof.app.core.model.*
import com.wipeproof.app.core.sanitization.MethodAssessment
import com.wipeproof.app.core.sanitization.SanitizationMethodSelector
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.data.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssessSanitizationUseCase @Inject constructor(
    private val selector: SanitizationMethodSelector,
    private val caseRepository: CaseRepository,
    private val eventRepository: EventRepository
) {

    suspend fun execute(
        case_: SanitizationCase,
        isOfficeKitConnected: Boolean = false
    ): List<MethodAssessment> {
        val assessments = selector.assess(case_.deviceInfo, isOfficeKitConnected)
        val recommended = assessments.firstOrNull()?.recommendedMethod

        val updated = case_.copy(
            status = CaseStatus.ASSESSED,
            selectedMethod = recommended,
            updatedAt = System.currentTimeMillis()
        )
        caseRepository.update(updated)

        eventRepository.recordEvent(
            caseId = case_.caseId,
            type = EventType.ASSESSMENT_COMPLETED,
            actorId = case_.deviceInfo.fingerprint.take(12),
            description = "Assessment complete. Selected method: ${recommended?.displayName ?: "NOT SUPPORTED"}",
            payload = "{\"method\":\"${recommended?.name}\",\"classification\":\"${recommended?.classification}\"}"
        )

        return assessments
    }
}
