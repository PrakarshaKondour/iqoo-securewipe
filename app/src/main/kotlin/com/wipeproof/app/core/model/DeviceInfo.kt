package com.wipeproof.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val serialNumber: String?,
    val assetId: String?,
    val androidVersion: String?,
    val buildId: String?,
    val securityPatchLevel: String?,
    val storageDescriptors: List<StorageDescriptor>,
    val fingerprint: String
)

@Serializable
data class StorageDescriptor(
    val label: String,
    val type: StorageType,
    val totalBytes: Long?,
    val availableBytes: Long?,
    val isRemovable: Boolean,
    val path: String?,
    val isEncrypted: Boolean
)

@Serializable
enum class StorageType {
    INTERNAL_UFS,
    INTERNAL_EMMC,
    EXTERNAL_SD,
    EXTERNAL_USB,
    NVME,
    HDD,
    SSD,
    UNKNOWN
}
