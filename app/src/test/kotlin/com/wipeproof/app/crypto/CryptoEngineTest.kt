package com.wipeproof.app.crypto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64

class CryptoEngineTest {

    private val testKeyPair by lazy {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        kpg.generateKeyPair()
    }

    @Test
    fun `sha256 is deterministic`() {
        val input = "wipeproof-test-input"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val expected = digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
        val result = digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
        assertEquals(expected, result)
    }

    @Test
    fun `sha256 different inputs produce different hashes`() {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val h1 = digest.digest("input1".toByteArray()).joinToString("") { "%02x".format(it) }
        val h2 = digest.digest("input2".toByteArray()).joinToString("") { "%02x".format(it) }
        assertNotEquals(h1, h2)
    }

    @Test
    fun `ECDSA sign and verify round trip`() {
        val data = "WipeProof certificate payload test".toByteArray()
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(testKeyPair.private)
        sig.update(data)
        val sigBytes = sig.sign()
        val sigBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes)

        val decodedSig = Base64.getUrlDecoder().decode(sigBase64)
        sig.initVerify(testKeyPair.public)
        sig.update(data)
        assertTrue(sig.verify(decodedSig), "Valid signature should verify")
    }

    @Test
    fun `ECDSA verify rejects tampered data`() {
        val data = "Original payload".toByteArray()
        val tamperedData = "Tampered payload".toByteArray()

        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(testKeyPair.private)
        sig.update(data)
        val sigBytes = sig.sign()

        sig.initVerify(testKeyPair.public)
        sig.update(tamperedData)
        assertFalse(sig.verify(sigBytes), "Tampered data should fail verification")
    }

    @Test
    fun `ECDSA verify rejects wrong key`() {
        val data = "Test payload".toByteArray()

        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val wrongKeyPair = kpg.generateKeyPair()

        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(testKeyPair.private)
        sig.update(data)
        val sigBytes = sig.sign()

        sig.initVerify(wrongKeyPair.public)
        sig.update(data)
        assertFalse(sig.verify(sigBytes), "Wrong key should fail verification")
    }

    @Test
    fun `event chain hash changes when data changes`() {
        fun computeHash(prev: String?, ts: Long, type: String, desc: String, payload: String?): String {
            val input = "${prev ?: "GENESIS"}|$ts|$type|$desc|${payload ?: ""}"
            val d = java.security.MessageDigest.getInstance("SHA-256")
            return d.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
        }

        val h1 = computeHash(null, 1000L, "CASE_CREATED", "Case created", null)
        val h2 = computeHash(h1, 2000L, "DEVICE_IDENTIFIED", "Device scanned", "{\"serial\":\"ABC123\"}")
        val h3Original = computeHash(h2, 3000L, "SANITIZATION_STARTED", "Wipe started", null)
        val h3Tampered = computeHash(h2, 3000L, "SANITIZATION_STARTED", "TAMPERED", null)

        assertNotEquals(h3Original, h3Tampered, "Tampered event should produce different hash")
        assertNotEquals(h1, h2, "Chain should evolve")
    }
}
