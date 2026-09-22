package com.wipeproof.app.data.db.dao

import androidx.room.*
import com.wipeproof.app.data.db.entity.CaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :id")
    suspend fun getById(id: String): CaseEntity?

    @Query("SELECT * FROM cases WHERE caseId = :caseId")
    suspend fun getByCaseId(caseId: String): CaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(case_: CaseEntity)

    @Update
    suspend fun update(case_: CaseEntity)

    @Query("DELETE FROM cases WHERE id = :id")
    suspend fun deleteById(id: String)
}
