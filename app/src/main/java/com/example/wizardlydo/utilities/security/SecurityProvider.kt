package com.example.wizardlydo.utilities.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class SecurityProvider(private val context: Context) {
    private val keyAlias = "WizardAppSecurityKey"

    private fun getKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore
    }

    private fun createEncryptionKey() {
        val keyStore = getKeyStore()
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun encrypt(value: String): String {
        createEncryptionKey()

        val keyStore = getKeyStore()
        val secretKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val combinedBytes = cipher.iv + encryptedBytes

        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    fun decrypt(encryptedValue: String): String {
        val keyStore = getKeyStore()
        val secretKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val combinedBytes = Base64.decode(encryptedValue, Base64.DEFAULT)
        val iv = combinedBytes.copyOfRange(0, 12)
        val encryptedBytes = combinedBytes.copyOfRange(12, combinedBytes.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}