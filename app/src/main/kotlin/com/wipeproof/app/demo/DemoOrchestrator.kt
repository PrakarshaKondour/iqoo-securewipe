package com.wipeproof.app.demo

import com.wipeproof.app.core.crypto.CertificateBuilder
import com.wipeproof.app.core.crypto.CryptoEngine
import com.wipeproof.app.core.model.*
import com.wipeproof.app.domain.usecase.CreateCaseUseCase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoOrchestrator @Inject constructor(
    private val createCaseUseCase: CreateCaseUseCase,
    private val cryptoEngine: CryptoEngine,
    private val certificateBuilder: CertificateBuilder
) {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun createDemoDeviceInfo(): DeviceInfo {
        val storage = listOf(
            StorageDescriptor(
                label = "UFS 4.0 Internal Flash (iQOO 12)",
                type = StorageType.INTERNAL_UFS,
                totalBytes = 256_000_000_000L,
                availableBytes = 184_000_000_000L,
                isRemovable = false,
                path = "/data",
                isEncrypted = true
            ),
            StorageDescriptor(
                label = "SanDisk Extreme MicroSD",
                type = StorageType.EXTERNAL_SD,
                totalBytes = 128_000_000_000L,
                availableBytes = 127_000_000_000L,
                isRemovable = true,
                path = "/storage/sdcard1",
                isEncrypted = false
            )
        )

        val rawFp = "vivo|iQOO 12 Pro|IQOO-DEMO-2024-X99|OriginOS 4|FBE-ENCRYPTED"
        val fp = cryptoEngine.sha256Hex(rawFp)

        return DeviceInfo(
            manufacturer = "iQOO",
            model = "iQOO 12 (Snapdragon 8 Gen 3)",
            serialNumber = "IQOO-DEMO-2024-X99",
            assetId = "CORP-ASSET-77291",
            androidVersion = "Android 14 (OriginOS 4 / Funtouch)",
            buildId = "V2307A_14.0.12.0.W10.V000L1",
            securityPatchLevel = "2024-08-01",
            storageDescriptors = storage,
            fingerprint = fp
        )
    }

    suspend fun setupDemoCase(): SanitizationCase {
        val demoDevice = createDemoDeviceInfo()
        return createCaseUseCase.execute(demoDevice, isDemoMode = true)
    }

    fun tamperCertificate(signedCert: SignedCertificate): String {
        // Altering one field (e.g. changing outcome to FAILED or modifying device model)
        // without updating the digital signature breaks verification!
        val tamperedCert = signedCert.certificate.copy(
            deviceModel = "${signedCert.certificate.deviceModel} [TAMPERED_IN_TRANSIT]",
            verificationOutcome = "FORGED_SUCCESS"
        )
        val tamperedSignedCert = SignedCertificate(
            certificate = tamperedCert,
            signatureBase64 = signedCert.signatureBase64 // Keeping original signature
        )
        return certificateBuilder.encodeToQrPayload(tamperedSignedCert)
    }
}
