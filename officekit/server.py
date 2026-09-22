#!/usr/bin/env python3
"""
WipeProof Office Kit Server
Runs on laptop/desktop privileged environment.
Binds strictly to 127.0.0.1:8765.
Communicates with WipeProof Android app over USB ADB port-forwarding:
    adb forward tcp:8765 tcp:8765
"""

import threading
import time
import uuid
from flask import Flask, jsonify, request
from sanitize.linux_shred import check_tools, list_block_devices, run_blkdiscard, run_shred
from evidence import to_wipeproof_hashes

app = Flask(__name__)

# In-memory operation tracking
operations = {}


@app.route("/ping", methods=["GET"])
def ping():
    return jsonify({
        "status": "ok",
        "service": "WipeProof Office Kit",
        "version": "1.0.0",
        "tools": check_tools()
    })


@app.route("/devices", methods=["GET"])
def get_devices():
    devices = list_block_devices()
    return jsonify(devices)


@app.route("/sanitize", methods=["POST"])
def start_sanitize():
    data = request.get_json() or {}
    device_path = data.get("device", "/dev/sdX")
    method = data.get("method", "OFFICE_KIT_SHRED")

    # Safety guard: prevent accidental system drive overwrite
    if device_path in ["/", "/dev/sda", "C:\\"]:
        return jsonify({"error": "Cannot sanitize primary system volume"}), 400

    op_id = str(uuid.uuid4())
    operations[op_id] = {
        "operationId": op_id,
        "device": device_path,
        "method": method,
        "status": "RUNNING",
        "percentage": 5,
        "step": "Starting sanitization worker...",
        "evidence": None
    }

    def worker():
        def progress(pct, msg):
            operations[op_id]["percentage"] = pct
            operations[op_id]["step"] = msg

        if method == "OFFICE_KIT_BLKDISCARD":
            res = run_blkdiscard(device_path, progress_callback=progress)
        else:
            res = run_shred(device_path, passes=3, progress_callback=progress)

        operations[op_id]["status"] = "COMPLETED" if res.get("success") else "FAILED"
        operations[op_id]["percentage"] = 100
        operations[op_id]["evidence"] = res

    thread = threading.Thread(target=worker, daemon=True)
    thread.start()

    return jsonify({
        "operationId": op_id,
        "status": "started"
    })


@app.route("/status/<op_id>", methods=["GET"])
def get_status(op_id):
    op = operations.get(op_id)
    if not op:
        return jsonify({"error": "Operation not found"}), 404
    return jsonify({
        "operationId": op_id,
        "status": op["status"],
        "percentage": op["percentage"],
        "step": op["step"]
    })


@app.route("/evidence/<op_id>", methods=["GET"])
def get_evidence(op_id):
    op = operations.get(op_id)
    if not op or not op.get("evidence"):
        return jsonify({"error": "Evidence not ready"}), 404

    raw = op["evidence"]
    hash_payloads = to_wipeproof_hashes(raw)

    return jsonify({
        "operationId": op_id,
        "method": raw.get("method", "OFFICE_KIT_SHRED"),
        "targetDevice": raw.get("targetDevice", ""),
        "startedAt": raw.get("startedAt", 0),
        "completedAt": raw.get("completedAt", 0),
        "passCount": raw.get("passes", 1),
        "evidenceHashes": hash_payloads,
        "success": raw.get("success", False)
    })


if __name__ == "__main__":
    print("=====================================================")
    print("  WipeProof Office Kit Server v1.0.0")
    print("  Privileged Laptop Sanitization Bridge")
    print("  Binding to http://127.0.0.1:8765")
    print("  Forward on Android via: adb forward tcp:8765 tcp:8765")
    print("=====================================================")
    app.run(host="127.0.0.1", port=8765, debug=False)
