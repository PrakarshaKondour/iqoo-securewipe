package com.wipeproof.app.domain.usecase

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import com.wipeproof.app.core.crypto.CryptoEngine
import com.wipeproof.app.core.model.DeviceInfo
import com.wipeproof.app.core.model.StorageDescriptor
import com.wipeproof.app.core.model.StorageType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentifyDeviceUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoEngine: CryptoEngine
) {

    fun execute(scannedAssetId: String? = null): DeviceInfo {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val serial = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        } catch (e: SecurityException) {
            "RESTRICTED-${Build.ID.take(8)}"
        }

        val storageDescriptors = mutableListOf<StorageDescriptor>()

        // 1. Internal Flash
        val dataDir = Environment.getDataDirectory()
        val totalInternal = dataDir.totalSpace
        val freeInternal = dataDir.freeSpace
        storageDescriptors.add(
            StorageDescriptor(
                label = "Internal Flash Storage (eMMC/UFS)",
                type = StorageType.INTERNAL_UFS,
                totalBytes = totalInternal,
                availableBytes = freeInternal,
                isRemovable = false,
                path = dataDir.absolutePath,
                isEncrypted = true
            )
        )

        // 2. Query removable / external volumes
        try {
            val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes = sm.storageVolumes
            for (vol in storageVolumes) {
                if (vol.isRemovable) {
                    val label = vol.getDescription(context) ?: "External Media"
                    storageDescriptors.add(
                        StorageDescriptor(
                            label = label,
                            type = StorageType.EXTERNAL_SD,
                            totalBytes = null,
                            availableBytes = null,
                            isRemovable = true,
                            path = null,
                            isEncrypted = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // StorageManager inspection fallback
        }

        val fingerprintRaw = "$manufacturer|$model|$serial|${Build.DISPLAY}|${Build.FINGERPRINT}"
        val fingerprint = cryptoEngine.sha256Hex(fingerprintRaw)

        return DeviceInfo(
            manufacturer = manufacturer,
            model = model,
            serialNumber = serial,
            assetId = scannedAssetId,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            buildId = Build.DISPLAY,
            securityPatchLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            storageDescriptors = storageDescriptors,
            fingerprint = fingerprint
        )
    }
}
