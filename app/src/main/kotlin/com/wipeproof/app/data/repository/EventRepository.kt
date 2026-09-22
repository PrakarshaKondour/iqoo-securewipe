package com.wipeproof.app.data.repository

import com.wipeproof.app.core.crypto.CryptoEngine
import com.wipeproof.app.core.model.EventType
import com.wipeproof.app.core.model.SanitizationEvent
import com.wipeproof.app.data.db.dao.EventDao
import com.wipeproof.app.data.db.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val cryptoEngine: CryptoEngine
) {

    fun observeEventsForCase(caseId: String): Flow<List<SanitizationEvent>> {
        return eventDao.observeByCaseId(caseId).map { entities ->
            entities.map { toDomain(it) }
        }
    }

    suspend fun getEventsForCase(caseId: String): List<SanitizationEvent> {
        return eventDao.getByCaseId(caseId).map { toDomain(it) }
    }

    suspend fun recordEvent(
        id: String = java.util.UUID.randomUUID().toString(),
        caseId: String,
        type: EventType,
        actorId: String,
        description: String,
        payload: String? = null
    ): SanitizationEvent {
        val latest = eventDao.getLatestForCase(caseId)
        val prevHash = latest?.hash
        val timestamp = System.currentTimeMillis()
        val hash = cryptoEngine.computeEventHash(prevHash, timestamp, type.name, description, payload)

        val entity = EventEntity(
            id = id,
            caseId = caseId,
            type = type.name,
            timestamp = timestamp,
            actorId = actorId,
            description = description,
            payload = payload,
            hash = hash,
            previousHash = prevHash
        )
        eventDao.insert(entity)
        return toDomain(entity)
    }

    suspend fun verifyChainIntegrity(caseId: String): Boolean {
        val events = eventDao.getByCaseId(caseId)
        if (events.isEmpty()) return true

        var expectedPrevHash: String? = null
        for (event in events) {
            if (event.previousHash != expectedPrevHash) {
                return false
            }
            val computedHash = cryptoEngine.computeEventHash(
                event.previousHash,
                event.timestamp,
                event.type,
                event.description,
                event.payload
            )
            if (computedHash != event.hash) {
                return false
            }
            expectedPrevHash = event.hash
        }
        return true
    }

    private fun toDomain(entity: EventEntity): SanitizationEvent {
        val type = try {
            EventType.valueOf(entity.type)
        } catch (e: Exception) {
            EventType.SANITIZATION_PROGRESS
        }
        return SanitizationEvent(
            id = entity.id,
            caseId = entity.caseId,
            type = type,
            timestamp = entity.timestamp,
            actorId = entity.actorId,
            description = entity.description,
            payload = entity.payload,
            hash = entity.hash,
            previousHash = entity.previousHash
        )
    }
}
