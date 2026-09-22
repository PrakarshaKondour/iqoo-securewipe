# WipeProof (SIH25070)
### *"Don't trust the wipe. Verify it."*

WipeProof is a production-grade Android-first platform for cryptographically verifiable device sanitization. It eliminates blind trust from device decommissioning by producing tamper-evident digital certificates verified independently by second-party devices.

---

## 🎯 The Core Flow

```
IDENTIFY ──► ASSESS ──► SANITIZE ──► VERIFY ──► PROVE ──► HANDOVER
```

1. **IDENTIFY**: Camera scanner for asset tags + Android Build APIs + StorageManager volume discovery. Creates Case ID (`WP-YYYYMMDD-XXXX`) and device hardware fingerprint.
2. **ASSESS**: Maps media (UFS, eMMC, SD, NVMe, HDD) to NIST SP 800-88 Clear, Purge, or Destroy methods. Explains technical OS boundaries in plain language.
3. **SANITIZE**: Guided, auditable sanitization. Respects Android sandboxing (FBE crypto-erase), direct 3-pass overwrite for SD cards, and Office Kit bridge for PC external drives.
4. **VERIFY**: Captures deterministic technical evidence hashes (sector sampling, TEE key revocation states).
5. **PROVE**: Issues ECDSA P-256 digital certificate signed with hardware-backed Android Keystore. Renders offline verification QR.
6. **HANDOVER**: 5-point readiness checklist ("SAFE TO HAND OVER ✓") and tamper-proof SHA-256 event chain Sanitization Passport.

---

## 📱 The Killer Feature: Second-Party Verification

1. A device completes sanitization and generates a signed QR certificate.
2. An auditor or second party opens WipeProof's **"VERIFY CERTIFICATE"** mode on another phone.
3. Scanning the QR decompressess and mathematically verifies the ECDSA digital signature locally.
4. **Immediate visual verification**:
   - `VERIFIED ✓` (Green): Signature matches payload, zero alterations detected.
   - `INVALID / TAMPERED ✕` (Red): Signature mismatch, identifies modified fields.

---

## ⏱️ 90-Second Hackathon Demo Script

1. Launch WipeProof on the primary iQOO device.
2. Tap **"90-Second Hackathon Demo Mode"**.
3. **Stage 1 (Identify)**: Automatic discovery of iQOO 12 hardware and storage volumes.
4. **Stage 2 (Sanitize)**: Simulated 3-pass overwrite and sector entropy evidence generation.
5. **Stage 3 (Prove)**: ECDSA P-256 signed certificate and compressed QR code generated.
6. **Stage 4 (Verify)**: Tap "Scan as 2nd Party" -> Shows **`VERIFIED ✓`**.
7. **Stage 5 (Tamper)**: Tap "Tamper with Certificate & Re-Verify" -> Shows **`INVALID / TAMPERED ✕`** with clear explanation of signature violation!
8. View **Sanitization Passport** and **"SAFE TO HAND OVER ✓"** readiness checklist.

---

## 💻 Office Kit (Privileged Laptop Bridge)

For PC/laptop sanitization of external drives (shred, NVMe blkdiscard):

```bash
cd officekit
pip install -r requirements.txt
adb forward tcp:8765 tcp:8765
python server.py
```

The phone connects to `http://127.0.0.1:8765` over ADB port-forwarding, triggers sanitization, and receives streamed sector evidence hashes.

---

## 🛠️ Building & Installing the App

```bash
# Clone the repository
git clone <repo-url>
cd iqoo

# Build Debug APK
./gradlew assembleDebug

# Install onto connected iQOO device
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛡️ Technical Highlights & Integrity
- **Pure Android Keystore**: Private keys never leave the hardware TEE / StrongBox.
- **Offline Verifiability**: Verification operates fully disconnected from the internet.
- **Zero Fake Success**: Unsupported media is explicitly reported as `NOT_SUPPORTED` / Physical Destruction Required.
- **Privacy-Preserving**: No deleted user files or sensitive personal data are ever placed into certificates.
