package com.wipeproof.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val type: String,
    val timestamp: Long,
    val actorId: String,
    val description: String,
    val payload: String?,
    val hash: String,
    val previousHash: String?
)
