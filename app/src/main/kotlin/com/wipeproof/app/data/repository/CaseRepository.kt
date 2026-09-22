package com.wipeproof.app.data.repository

import com.wipeproof.app.core.model.CaseStatus
import com.wipeproof.app.core.model.DeviceInfo
import com.wipeproof.app.core.model.SanitizationCase
import com.wipeproof.app.core.model.SanitizationMethod
import com.wipeproof.app.data.db.dao.CaseDao
import com.wipeproof.app.data.db.entity.CaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaseRepository @Inject constructor(
    private val caseDao: CaseDao
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun observeAllCases(): Flow<List<SanitizationCase>> {
        return caseDao.observeAll().map { entities ->
            entities.map { toDomain(it) }
        }
    }

    suspend fun getById(id: String): SanitizationCase? {
        return caseDao.getById(id)?.let { toDomain(it) }
    }

    suspend fun getByCaseId(caseId: String): SanitizationCase? {
        return caseDao.getByCaseId(caseId)?.let { toDomain(it) }
    }

    suspend fun insert(case_: SanitizationCase) {
        caseDao.insert(toEntity(case_))
    }

    suspend fun update(case_: SanitizationCase) {
        caseDao.update(toEntity(case_))
    }

    suspend fun updateStatus(id: String, newStatus: CaseStatus) {
        caseDao.getById(id)?.let { entity ->
            val updated = entity.copy(status = newStatus.name, updatedAt = System.currentTimeMillis())
            caseDao.update(updated)
        }
    }

    private fun toDomain(entity: CaseEntity): SanitizationCase {
        val deviceInfo = json.decodeFromString<DeviceInfo>(entity.deviceInfoJson)
        val method = entity.selectedMethodName?.let {
            try {
                SanitizationMethod.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
        val status = try {
            CaseStatus.valueOf(entity.status)
        } catch (e: Exception) {
            CaseStatus.IDENTIFIED
        }
        return SanitizationCase(
            id = entity.id,
            caseId = entity.caseId,
            deviceInfo = deviceInfo,
            status = status,
            selectedMethod = method,
            isDemoMode = entity.isDemoMode,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: SanitizationCase): CaseEntity {
        return CaseEntity(
            id = domain.id,
            caseId = domain.caseId,
            deviceInfoJson = json.encodeToString(domain.deviceInfo),
            status = domain.status.name,
            selectedMethodName = domain.selectedMethod?.name,
            isDemoMode = domain.isDemoMode,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
