package com.wipeproof.app.data.repository

import com.wipeproof.app.core.model.SignedCertificate
import com.wipeproof.app.data.db.dao.CertificateDao
import com.wipeproof.app.data.db.entity.CertificateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateRepository @Inject constructor(
    private val certificateDao: CertificateDao
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun getByCaseId(caseId: String): Pair<SignedCertificate, String>? {
        val entity = certificateDao.getByCaseId(caseId) ?: return null
        val cert = json.decodeFromString<SignedCertificate>(entity.signedCertificateJson)
        return Pair(cert, entity.qrPayload)
    }

    fun observeAll(): Flow<List<Pair<SignedCertificate, String>>> {
        return certificateDao.observeAll().map { list ->
            list.map { entity ->
                val cert = json.decodeFromString<SignedCertificate>(entity.signedCertificateJson)
                Pair(cert, entity.qrPayload)
            }
        }
    }

    suspend fun saveCertificate(caseId: String, cert: SignedCertificate, qrPayload: String) {
        val jsonStr = json.encodeToString(cert)
        val entity = CertificateEntity(
            caseId = caseId,
            signedCertificateJson = jsonStr,
            qrPayload = qrPayload,
            createdAt = System.currentTimeMillis()
        )
        certificateDao.insert(entity)
    }
}
