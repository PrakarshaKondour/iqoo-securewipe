package com.wipeproof.app.core.sanitization

import com.wipeproof.app.core.model.*
import javax.inject.Inject
import javax.inject.Singleton

data class MethodAssessment(
    val storageDescriptor: StorageDescriptor,
    val recommendedMethod: SanitizationMethod,
    val alternativeMethods: List<SanitizationMethod>,
    val notes: List<String>,
    val canVerify: Boolean
)

@Singleton
class SanitizationMethodSelector @Inject constructor() {

    fun assess(deviceInfo: DeviceInfo, isOfficeKitConnected: Boolean): List<MethodAssessment> {
        return deviceInfo.storageDescriptors.map { storage ->
            assessStorage(storage, isOfficeKitConnected)
        }
    }

    private fun assessStorage(
        storage: StorageDescriptor,
        isOfficeKitConnected: Boolean
    ): MethodAssessment {
        return when (storage.type) {
            StorageType.INTERNAL_UFS, StorageType.INTERNAL_EMMC -> assessInternalAndroidStorage(storage)
            StorageType.EXTERNAL_SD -> assessSdCard(storage)
            StorageType.EXTERNAL_USB -> assessExternalUsb(storage, isOfficeKitConnected)
            StorageType.NVME -> assessNvme(storage, isOfficeKitConnected)
            StorageType.SSD -> assessSsd(storage, isOfficeKitConnected)
            StorageType.HDD -> assessHdd(storage, isOfficeKitConnected)
            StorageType.UNKNOWN -> notSupported(storage, "Storage type cannot be determined")
        }
    }

    private fun assessInternalAndroidStorage(storage: StorageDescriptor): MethodAssessment {
        val notes = mutableListOf<String>()
        notes.add("Android internal storage uses File-Based Encryption (FBE).")
        notes.add("Factory reset discards encryption keys in TEE/Keystore, making data unrecoverable.")
        if (storage.isEncrypted) {
            notes.add("Device reports hardware-backed encryption — factory reset achieves crypto-erase (NIST Clear class).")
        } else {
            notes.add("WARNING: Device does not report encryption. Factory reset may not achieve full crypto-erase. Physical destruction recommended for high-security requirements.")
        }
        notes.add("WipeProof respects Android security sandbox: internal flash cannot be blindly overwritten by non-root apps.")

        return MethodAssessment(
            storageDescriptor = storage,
            recommendedMethod = SanitizationMethod.FACTORY_RESET,
            alternativeMethods = emptyList(),
            notes = notes,
            canVerify = true
        )
    }

    private fun assessSdCard(storage: StorageDescriptor): MethodAssessment {
        return MethodAssessment(
            storageDescriptor = storage,
            recommendedMethod = SanitizationMethod.SD_OVERWRITE_3PASS,
            alternativeMethods = listOf(SanitizationMethod.SD_OVERWRITE_SINGLE),
            notes = listOf(
                "Removable SD flash media detected.",
                "3-pass overwrite conforms to NIST SP 800-88 Purge recommendations for flash.",
                "WipeProof directly overwrites accessible blocks and sample-verifies post-wipe.",
                "Note: Flash wear-leveling spare blocks require physical destruction for Top Secret compliance."
            ),
            canVerify = true
        )
    }

    private fun assessExternalUsb(storage: StorageDescriptor, officeKit: Boolean): MethodAssessment {
        return if (officeKit) {
            MethodAssessment(
                storageDescriptor = storage,
                recommendedMethod = SanitizationMethod.OFFICE_KIT_SHRED,
                alternativeMethods = listOf(SanitizationMethod.OFFICE_KIT_BLKDISCARD),
                notes = listOf(
                    "USB external mass storage detected. Office Kit connected.",
                    "shred / blkdiscard executed via privileged Office Kit bridge.",
                    "Evidence streamed back to this iQOO handset and cryptographically signed."
                ),
                canVerify = true
            )
        } else {
            notSupported(storage, "USB external storage requires Office Kit (laptop bridge). Connect Office Kit over ADB to proceed.")
        }
    }

    private fun assessNvme(storage: StorageDescriptor, officeKit: Boolean): MethodAssessment {
        return if (officeKit) {
            MethodAssessment(
                storageDescriptor = storage,
                recommendedMethod = SanitizationMethod.OFFICE_KIT_BLKDISCARD,
                alternativeMethods = listOf(SanitizationMethod.OFFICE_KIT_SHRED),
                notes = listOf(
                    "NVMe PCIe solid state drive detected. Office Kit connected.",
                    "blkdiscard --secure executes hardware crypto-erase / controller sanitization.",
                    "NIST SP 800-88 Purge class when Secure Erase is confirmed by NVMe controller."
                ),
                canVerify = true
            )
        } else {
            notSupported(storage, "NVMe storage requires Office Kit privileged environment. Connect Office Kit to proceed.")
        }
    }

    private fun assessSsd(storage: StorageDescriptor, officeKit: Boolean): MethodAssessment {
        return if (officeKit) {
            MethodAssessment(
                storageDescriptor = storage,
                recommendedMethod = SanitizationMethod.OFFICE_KIT_BLKDISCARD,
                alternativeMethods = listOf(SanitizationMethod.OFFICE_KIT_SHRED),
                notes = listOf(
                    "SATA SSD detected. Office Kit connected.",
                    "ATA Secure Erase / blkdiscard triggers internal block clearance.",
                    "Evidence package will be returned to iQOO for verification."
                ),
                canVerify = true
            )
        } else {
            notSupported(storage, "SATA SSD requires Office Kit connection.")
        }
    }

    private fun assessHdd(storage: StorageDescriptor, officeKit: Boolean): MethodAssessment {
        return if (officeKit) {
            MethodAssessment(
                storageDescriptor = storage,
                recommendedMethod = SanitizationMethod.OFFICE_KIT_SHRED,
                alternativeMethods = emptyList(),
                notes = listOf(
                    "Rotational hard disk drive detected. Office Kit connected.",
                    "GNU shred 3-pass overwrite (pseudorandom + zero) ensures magnetic domain sanitization.",
                    "Evidence hashes generated on completion."
                ),
                canVerify = true
            )
        } else {
            notSupported(storage, "HDD magnetic sanitization requires Office Kit.")
        }
    }

    private fun notSupported(storage: StorageDescriptor, reason: String): MethodAssessment {
        return MethodAssessment(
            storageDescriptor = storage,
            recommendedMethod = SanitizationMethod.NOT_SUPPORTED,
            alternativeMethods = emptyList(),
            notes = listOf(
                reason,
                "WipeProof refuses to report this media as sanitized.",
                "NIST SP 800-88 Destroy (physical destruction) is required."
            ),
            canVerify = false
        )
    }
}
