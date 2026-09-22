package com.wipeproof.app.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoEngine @Inject constructor() {

    companion object {
        private const val KEY_ALIAS = "wipeproof_signing_key_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val KEY_ALGORITHM = "EC"
        private const val CURVE_NAME = "secp256r1"
    }

    private var fallbackKeyPair: KeyPair? = null

    private fun isAndroidKeyStoreAvailable(): Boolean {
        return try {
            KeyStore.getInstance(ANDROID_KEYSTORE) != null
        } catch (e: Exception) {
            false
        }
    }

    fun ensureKeyPairExists() {
        if (isAndroidKeyStoreAvailable()) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                if (!keyStore.containsAlias(KEY_ALIAS)) {
                    val kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE
                    )
                    val spec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                    )
                        .setAlgorithmParameterSpec(ECGenParameterSpec(CURVE_NAME))
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setUserAuthenticationRequired(false)
                        .build()
                    kpg.initialize(spec)
                    kpg.generateKeyPair()
                }
                return
            } catch (e: Exception) {
                // Fallback to in-memory EC keypair if AndroidKeyStore initialization fails
            }
        }

        if (fallbackKeyPair == null) {
            val kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM)
            kpg.initialize(ECGenParameterSpec(CURVE_NAME))
            fallbackKeyPair = kpg.generateKeyPair()
        }
    }

    suspend fun sign(data: ByteArray): String = withContext(Dispatchers.IO) {
        ensureKeyPairExists()
        if (isAndroidKeyStoreAvailable()) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
                    val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
                    signature.initSign(privateKey)
                    signature.update(data)
                    val sigBytes = signature.sign()
                    return@withContext Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes)
                }
            } catch (e: Exception) {
                // Fallback
            }
        }

        val privateKey = fallbackKeyPair!!.private
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(data)
        val sigBytes = signature.sign()
        Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes)
    }

    fun verify(data: ByteArray, signatureBase64: String, publicKeyPem: String): Boolean {
        return try {
            val publicKey = pemToPublicKey(publicKeyPem)
            val sigBytes = Base64.getUrlDecoder().decode(signatureBase64)
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initVerify(publicKey)
            signature.update(data)
            signature.verify(sigBytes)
        } catch (e: Exception) {
            false
        }
    }

    fun getPublicKeyPem(): String {
        ensureKeyPairExists()
        val publicKey: PublicKey = if (isAndroidKeyStoreAvailable()) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                keyStore.getCertificate(KEY_ALIAS)?.publicKey ?: fallbackKeyPair!!.public
            } catch (e: Exception) {
                fallbackKeyPair!!.public
            }
        } else {
            fallbackKeyPair!!.public
        }

        val base64 = Base64.getEncoder().encodeToString(publicKey.encoded)
        return "-----BEGIN PUBLIC KEY-----\n" +
                base64.chunked(64).joinToString("\n") +
                "\n-----END PUBLIC KEY-----"
    }

    fun pemToPublicKey(pem: String): PublicKey {
        val stripped = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\r", "")
            .replace("\n", "")
            .trim()
        val keyBytes = Base64.getDecoder().decode(stripped)
        val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
    }

    fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    fun sha256Hex(text: String): String = sha256Hex(text.toByteArray(Charsets.UTF_8))

    fun computeEventHash(
        previousHash: String?,
        timestamp: Long,
        type: String,
        description: String,
        payload: String?
    ): String {
        val input = "${previousHash ?: "GENESIS"}|$timestamp|$type|$description|${payload ?: ""}"
        return sha256Hex(input.toByteArray(Charsets.UTF_8))
    }
}
