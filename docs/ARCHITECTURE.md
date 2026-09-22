# WipeProof Technical Architecture

> **"Don't trust the wipe. Verify it."** — Solving SIH25070

---

## 1. Core Workflow State Machine

WipeProof structures device sanitization into 6 sequential, auditable phases:

```
[ IDENTIFY ] ──► [ ASSESS ] ──► [ SANITIZE ] ──► [ VERIFY ] ──► [ PROVE ] ──► [ HANDOVER ]
```

1. **IDENTIFY**: Inspect hardware via Camera (QR/barcode asset tag), Android Build APIs, and StorageManager. Creates unique Case ID (`WP-YYYYMMDD-XXXX`) and device hardware fingerprint.
2. **ASSESS**: Evaluates storage boundaries (UFS, eMMC, SD, NVMe). Maps each volume to NIST SP 800-88 methods (Clear, Purge, Destroy).
3. **SANITIZE**: Guided execution respecting OS security sandboxes. Direct wipe for SD card, OS crypto-erase for Android internal storage, Office Kit for external drives.
4. **VERIFY**: Collects deterministic technical evidence hashes (sector sampling, TEE key revocation states).
5. **PROVE**: Assembles canonical certificate signed by hardware-backed ECDSA P-256 key in Android Keystore. Encodes certificate into high-density compressed QR.
6. **HANDOVER**: Validates 5-point readiness checklist and presents unbroken SHA-256 hash-chained Sanitization Passport.

---

## 2. Cryptographic Protocol

### Key Management
- **Key Type**: ECDSA with NIST P-256 curve (`secp256r1`).
- **Storage**: Hardware-backed Android Keystore (backed by TEE / StrongBox on iQOO handset).
- **Signing**: Done in-hardware. The private key never enters application memory.

### Canonical Signing Process
1. `SanitizationCertificate` struct assembled.
2. Serialized to deterministic JSON (sorted keys, no whitespace).
3. SHA-256 digest computed and signed via Android Keystore `Signature.getInstance("SHA256withECDSA")`.
4. Resulting DER signature is Base64url-encoded.
5. QR Payload: `Base64url( DEFLATE( JSON( SignedCertificate ) ) )`.

### Second-Party Offline Verification (Killer Feature)
1. Verifier phone scans QR code with camera (no network connection required).
2. Decompresses DEFLATE stream and parses `SignedCertificate`.
3. Extracts embedded issuer `PublicKeyPem`.
4. Reconstructs canonical JSON payload from certificate attributes.
5. Mathematically checks `Signature.verify(payloadBytes, signatureBytes)`.
6. Immediate high-contrast feedback:
   - **VERIFIED ✓** (Green): Payload is intact, signature valid.
   - **INVALID / TAMPERED ✕** (Red): Payload modified or forged.

---

## 3. Storage Sanitization Matrix

| Media Type | Recommended Method | Classification | NIST SP 800-88 Rev.1 | Verification Evidence |
|---|---|---|---|---|
| Android Internal (UFS/eMMC) | Factory Reset (FBE Crypto-Erase) | Clear (C) | Section 5.1.1 | Keystore key revocation, partition table digest |
| Removable MicroSD | 3-Pass Overwrite (0x00, 0xFF, Random) | Purge (P) | Section 5.1.2 | Sector sample hashes across LBA boundaries |
| External USB / HDD | Office Kit GNU shred | Purge (P) | Section 5.1.2 | Multi-pass execution telemetry & sector hashes |
| NVMe / SATA SSD | Office Kit blkdiscard --secure | Purge (P) | Section 5.1.3 | Discard table verification & sector sampling |
| Unknown / Legacy Media | Physical Destruction | Destroy (D) | Section 5.1.4 | Reported as "Not Supported / Destroy Required" |
