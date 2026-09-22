package com.wipeproof.app.data.db.dao

import androidx.room.*
import com.wipeproof.app.data.db.entity.CertificateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates WHERE caseId = :caseId")
    suspend fun getByCaseId(caseId: String): CertificateEntity?

    @Query("SELECT * FROM certificates ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cert: CertificateEntity)
}
