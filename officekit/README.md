# WipeProof Office Kit

### Privileged Desktop / Bootable Sanitization Bridge for WipeProof

WipeProof Office Kit is a lightweight service that runs on a technician's laptop or bootable Linux environment. It provides privileged sanitization access for external hard drives, NVMe/SATA SSDs, and mass-storage devices that mobile operating systems cannot directly overwrite.

---

## Architecture & Communication Flow

```
[ iQOO Android Phone ]
        │
   USB Tether / ADB
        │
        ▼  `adb forward tcp:8765 tcp:8765`
[ 127.0.0.1:8765 ]
        │
[ Office Kit Flask Server ]
        │
        ├─► GNU shred (NIST SP 800-88 Purge 3-Pass Overwrite)
        ├─► blkdiscard --secure (NVMe / ATA Secure Erase)
        └─► Sector Sampling Engine (SHA-256 Digest Extraction)
```

---

## Quickstart

### 1. Install Dependencies
```bash
cd officekit
pip install -r requirements.txt
```

### 2. Forward ADB Port to Connected iQOO Phone
```bash
adb forward tcp:8765 tcp:8765
```

### 3. Run Server (Run as root/administrator for direct disk access)
```bash
python server.py
# On Linux for raw block device access:
# sudo python server.py
```

---

## API Endpoints

- `GET /ping` — Health check, system utility discovery (`shred`, `blkdiscard`, etc.)
- `GET /devices` — List attached block devices (`lsblk` structured JSON)
- `POST /sanitize` — Initiate sanitization job (`{"device": "/dev/sdb", "method": "OFFICE_KIT_SHRED"}`)
- `GET /status/<op_id>` — Monitor progress percentage and current step
- `GET /evidence/<op_id>` — Retrieve structured SHA-256 evidence package
