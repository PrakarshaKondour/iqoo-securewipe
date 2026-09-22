package com.wipeproof.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wipeproof.app.data.db.dao.*
import com.wipeproof.app.data.db.entity.*

@Database(
    entities = [CaseEntity::class, EventEntity::class, CertificateEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WipeProofDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao
    abstract fun eventDao(): EventDao
    abstract fun certificateDao(): CertificateDao
}
