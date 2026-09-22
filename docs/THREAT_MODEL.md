# WipeProof Threat Model & Security Analysis

### Scope: SIH25070 Verifiable Device Sanitization

---

## 1. Protected Assets

- **Integrity of Sanitization Proof**: Prevention of fraudulent or altered wiping certificates.
- **Custody Audit Chain**: Prevention of retroactive tampering with the Sanitization Passport event history.
- **Hardware Identity Linkage**: Ensuring certificates cannot be transplanted across different devices.
- **User Privacy**: Guaranteeing no deleted or sensitive user data is ever leaked or stored in proof records.

---

## 2. Threat Actors & Attack Vectors

### Attack 1: Certificate Forgery / Modification
- **Threat**: An adversary alters the device serial, timestamp, or verification outcome in a certificate to claim a compromised device is sanitized.
- **Mitigation**: The certificate payload is protected by an ECDSA P-256 digital signature. Any single-bit alteration in the payload causes signature verification to fail immediately (`TAMPER DETECTED ✕`).

### Attack 2: Replay / Certificate Transplantation
- **Threat**: Reusing a valid certificate from Device A for an un-wiped Device B.
- **Mitigation**: Each certificate binds the unique hardware fingerprint (SHA-256 of manufacturer, model, serial, SoC build ID, and Keystore issuer key). A second party comparing the device identity against the certificate immediately identifies the mismatch.

### Attack 3: Fake "Successful Wipe" Claims
- **Threat**: Software declaring success on storage it cannot actually access or verify (e.g. non-root apps claiming raw flash overwrites).
- **Mitigation**: WipeProof explicitly enforces OS boundary honesty. Where direct access is restricted (Android internal UFS), it enforces OS Factory Reset with TEE crypto-erase. Unverifiable storage is tagged `NOT_SUPPORTED`, requiring physical destruction.

### Attack 4: Key Extraction
- **Threat**: Extracting private signing keys to forge certificates offline.
- **Mitigation**: Keys are generated inside the hardware-backed Android Keystore with `KeyGenParameterSpec` non-exportable flags. Private key material never enters RAM or disk.

---

## 3. Acknowledged Limitations & Physical Bounds

1. **Flash Wear-Leveling Spare Blocks**: Flash controllers maintain hidden reserve blocks for wear leveling. Software overwrites cannot guarantee 100% of reallocated physical cells are erased without hardware ATA Secure Erase or crypto-erase.
2. **Device Owner Requirement for Silent Reset**: Standard Android security prevents background apps from triggering factory resets without user confirmation unless provisioned as an enterprise Device Owner. WipeProof provides guided intent execution.
