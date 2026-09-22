package com.wipeproof.app.domain.usecase

import com.wipeproof.app.core.model.*
import com.wipeproof.app.data.repository.CaseRepository
import com.wipeproof.app.data.repository.EventRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateCaseUseCase @Inject constructor(
    private val caseRepository: CaseRepository,
    private val eventRepository: EventRepository
) {

    suspend fun execute(deviceInfo: DeviceInfo, isDemoMode: Boolean = false): SanitizationCase {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomSuffix = UUID.randomUUID().toString().take(4).uppercase()
        val caseId = "WP-$dateStr-$randomSuffix"
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val case_ = SanitizationCase(
            id = id,
            caseId = caseId,
            deviceInfo = deviceInfo,
            status = CaseStatus.IDENTIFIED,
            selectedMethod = null,
            isDemoMode = isDemoMode,
            createdAt = now,
            updatedAt = now
        )

        caseRepository.insert(case_)

        eventRepository.recordEvent(
            caseId = caseId,
            type = EventType.CASE_CREATED,
            actorId = deviceInfo.fingerprint.take(12),
            description = "Sanitization case initialized for ${deviceInfo.manufacturer} ${deviceInfo.model}",
            payload = "{\"caseId\":\"$caseId\",\"isDemoMode\":$isDemoMode}"
        )

        eventRepository.recordEvent(
            caseId = caseId,
            type = EventType.DEVICE_IDENTIFIED,
            actorId = deviceInfo.fingerprint.take(12),
            description = "Device hardware identity captured: S/N ${deviceInfo.serialNumber ?: "Unknown"}",
            payload = "{\"fingerprint\":\"${deviceInfo.fingerprint}\",\"model\":\"${deviceInfo.model}\"}"
        )

        return case_
    }
}
