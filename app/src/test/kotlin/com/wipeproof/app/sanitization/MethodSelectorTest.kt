package com.wipeproof.app.sanitization

import com.wipeproof.app.core.model.*
import com.wipeproof.app.core.sanitization.SanitizationMethodSelector
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MethodSelectorTest {
    private val selector = SanitizationMethodSelector()

    private fun makeDevice(storageType: StorageType, isEncrypted: Boolean = true): DeviceInfo {
        return DeviceInfo(
            manufacturer = "Test",
            model = "TestDevice",
            serialNumber = "SN12345",
            assetId = null,
            androidVersion = "14",
            buildId = "BUILD123",
            securityPatchLevel = "2024-01",
            storageDescriptors = listOf(
                StorageDescriptor(
                    label = "Internal Storage",
                    type = storageType,
                    totalBytes = 128_000_000_000L,
                    availableBytes = 50_000_000_000L,
                    isRemovable = false,
                    path = "/data",
                    isEncrypted = isEncrypted
                )
            ),
            fingerprint = "test-fingerprint"
        )
    }

    @Test
    fun `internal UFS maps to FACTORY_RESET`() {
        val device = makeDevice(StorageType.INTERNAL_UFS)
        val assessments = selector.assess(device, isOfficeKitConnected = false)
        assertEquals(SanitizationMethod.FACTORY_RESET, assessments.first().recommendedMethod)
    }

    @Test
    fun `external SD maps to 3-pass overwrite`() {
        val device = makeDevice(StorageType.EXTERNAL_SD)
        val assessments = selector.assess(device, isOfficeKitConnected = false)
        assertEquals(SanitizationMethod.SD_OVERWRITE_3PASS, assessments.first().recommendedMethod)
    }

    @Test
    fun `HDD without office kit returns NOT_SUPPORTED`() {
        val device = makeDevice(StorageType.HDD)
        val assessments = selector.assess(device, isOfficeKitConnected = false)
        assertEquals(SanitizationMethod.NOT_SUPPORTED, assessments.first().recommendedMethod)
    }

    @Test
    fun `HDD with office kit returns shred`() {
        val device = makeDevice(StorageType.HDD)
        val assessments = selector.assess(device, isOfficeKitConnected = true)
        assertEquals(SanitizationMethod.OFFICE_KIT_SHRED, assessments.first().recommendedMethod)
    }

    @Test
    fun `NVMe with office kit returns blkdiscard`() {
        val device = makeDevice(StorageType.NVME)
        val assessments = selector.assess(device, isOfficeKitConnected = true)
        assertEquals(SanitizationMethod.OFFICE_KIT_BLKDISCARD, assessments.first().recommendedMethod)
    }

    @Test
    fun `unencrypted internal storage has warning note`() {
        val device = makeDevice(StorageType.INTERNAL_UFS, isEncrypted = false)
        val assessment = selector.assess(device, false).first()
        assertTrue(assessment.notes.any { it.contains("WARNING") })
    }
}
