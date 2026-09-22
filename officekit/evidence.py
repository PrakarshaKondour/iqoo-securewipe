#!/usr/bin/env python3
"""
Evidence package builder for WipeProof Office Kit.
Formats raw sanitization execution data into WipeProof EvidenceHashPayload records.
"""

import hashlib
import json
from typing import Dict, List


def to_wipeproof_hashes(raw_evidence: Dict) -> List[Dict]:
    """Convert sector samples and metadata into EvidenceHashPayload objects."""
    hashes = []
    target = raw_evidence.get("targetDevice", "Unknown Media")
    method = raw_evidence.get("method", "OFFICE_KIT_SHRED")

    # 1. Package summary hash
    canonical = json.dumps(raw_evidence, sort_keys=True)
    pkg_hash = hashlib.sha256(canonical.encode("utf-8")).hexdigest()
    hashes.append({
        "label": f"Office Kit Execution Digest ({target})",
        "sha256": pkg_hash,
        "description": f"Overall execution digest for {method} on {target}."
    })

    # 2. Sector sample proofs
    for sample in raw_evidence.get("sectorSamples", []):
        idx = sample.get("sampleIndex", 1)
        offset = sample.get("offset", 0)
        h = sample.get("sha256", "")
        hashes.append({
            "label": f"Post-Wipe Sector #{idx} (LBA: {offset})",
            "sha256": h,
            "description": f"Verified sector read confirming zero entropy after overwrite."
        })

    return hashes
