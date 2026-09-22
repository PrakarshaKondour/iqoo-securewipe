package com.wipeproof.app.data.db.dao

import androidx.room.*
import com.wipeproof.app.data.db.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE caseId = :caseId ORDER BY timestamp ASC")
    fun observeByCaseId(caseId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE caseId = :caseId ORDER BY timestamp ASC")
    suspend fun getByCaseId(caseId: String): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM events WHERE caseId = :caseId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestForCase(caseId: String): EventEntity?
}
