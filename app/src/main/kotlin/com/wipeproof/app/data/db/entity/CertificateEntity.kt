package com.wipeproof.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey val caseId: String,
    val signedCertificateJson: String,
    val qrPayload: String,
    val createdAt: Long
)
