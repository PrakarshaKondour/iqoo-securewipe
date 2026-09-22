package com.wipeproof.app.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SanitizationClassification(val label: String, val color: Long) {
    CLEAR("CLEAR", 0xFF2196F3),
    PURGE("PURGE", 0xFF9C27B0),
    DESTROY("DESTROY", 0xFFFF5722),
    DEMO("DEMO", 0xFFFF9800),
    UNKNOWN("UNKNOWN", 0xFF9E9E9E)
}

@Serializable
enum class SanitizationMethod(
    val displayName: String,
    val classification: SanitizationClassification,
    val shortDescription: String,
    val detailedDescription: String,
    val nistReference: String?,
    val requiresOfficeKit: Boolean
) {
    FACTORY_RESET(
        displayName = "Android Factory Reset",
        classification = SanitizationClassification.CLEAR,
        shortDescription = "Wipes user data partition via Android OS.",
        detailedDescription = "Android factory reset clears the user data partition and triggers crypto-erase on UFS/eMMC storage. On modern Android devices with hardware-backed file-based encryption (FBE), the encryption keys are discarded making data unrecoverable. This meets NIST SP 800-88 Clear guidelines for internal Android storage.",
        nistReference = "NIST SP 800-88 Rev.1 — Clear (C)",
        requiresOfficeKit = false
    ),
    SD_OVERWRITE_3PASS(
        displayName = "SD Card 3-Pass Overwrite",
        classification = SanitizationClassification.PURGE,
        shortDescription = "Three-pass overwrite: zeros, ones, random data.",
        detailedDescription = "Performs three sequential overwrite passes on the SD card: all zeros, all ones, then cryptographically random data. Each pass is verified. Meets NIST SP 800-88 Purge guidelines for removable flash media.",
        nistReference = "NIST SP 800-88 Rev.1 — Purge (P)",
        requiresOfficeKit = false
    ),
    SD_OVERWRITE_SINGLE(
        displayName = "SD Card Single-Pass Zero Overwrite",
        classification = SanitizationClassification.CLEAR,
        shortDescription = "Single-pass zero overwrite of removable SD card.",
        detailedDescription = "Writes zeros to all accessible blocks of the SD card in a single pass. Meets NIST SP 800-88 Clear for removable media. Faster than 3-pass but lower assurance.",
        nistReference = "NIST SP 800-88 Rev.1 — Clear (C)",
        requiresOfficeKit = false
    ),
    OFFICE_KIT_SHRED(
        displayName = "Office Kit: shred (3-pass + zero)",
        classification = SanitizationClassification.PURGE,
        shortDescription = "Linux shred: 3 overwrite passes + final zero via laptop.",
        detailedDescription = "Uses the GNU shred utility on the connected laptop to perform 3 random overwrite passes followed by a final zero pass. Appropriate for HDDs and older SSDs. Evidence is cryptographically hashed and streamed back to this device for verification.",
        nistReference = "NIST SP 800-88 Rev.1 — Purge (P)",
        requiresOfficeKit = true
    ),
    OFFICE_KIT_BLKDISCARD(
        displayName = "Office Kit: Secure Erase / blkdiscard",
        classification = SanitizationClassification.PURGE,
        shortDescription = "NVMe/SSD ATA Secure Erase via laptop.",
        detailedDescription = "Uses blkdiscard --secure (NVMe/SSD) or ATA Secure Erase via hdparm on the connected laptop. For self-encrypting drives (SED), this performs crypto-erase: the media encryption key is discarded, making all data permanently unrecoverable without re-writing. Evidence is hashed and verified by this device.",
        nistReference = "NIST SP 800-88 Rev.1 — Purge (P)",
        requiresOfficeKit = true
    ),
    DEMO(
        displayName = "Demo Mode (Simulated)",
        classification = SanitizationClassification.DEMO,
        shortDescription = "DEMO: Simulated sanitization. NOT a real wipe.",
        detailedDescription = "This is a demonstration-only simulation. No actual data is wiped. All evidence, hashes, and results are synthetically generated for the purpose of demonstrating the WipeProof workflow. Demo certificates are clearly labeled and must not be used for compliance purposes.",
        nistReference = null,
        requiresOfficeKit = false
    ),
    NOT_SUPPORTED(
        displayName = "Not Supported",
        classification = SanitizationClassification.UNKNOWN,
        shortDescription = "No supported sanitization method for this storage.",
        detailedDescription = "WipeProof cannot perform a verifiable sanitization of this storage type. Physical destruction per NIST SP 800-88 Destroy guidelines is recommended. Attempting software sanitization on this media would not provide verifiable assurance.",
        nistReference = "NIST SP 800-88 Rev.1 — Destroy (D) recommended",
        requiresOfficeKit = false
    )
}
