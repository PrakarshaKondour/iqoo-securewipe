package com.wipeproof.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val deviceInfoJson: String,
    val status: String,
    val selectedMethodName: String?,
    val isDemoMode: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
