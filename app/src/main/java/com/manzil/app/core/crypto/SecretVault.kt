package com.manzil.app.core.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.manzil.app.core.logging.AppLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The API key never sits in plain text: it is encrypted with an AES-256-GCM key that
 * lives in the Android Keystore (which cannot be exported from the device) and the
 * ciphertext is kept in a DataStore file that is excluded from backups and exports.
 */
interface SecretVault {
    suspend fun saveApiKey(key: String)
    suspend fun getApiKey(): String?
    suspend fun hasApiKey(): Boolean
    suspend fun clearApiKey()
    suspend fun saveModel(model: String)
    suspend fun getModel(): String?
}

private val Context.secretDataStore: DataStore<Preferences> by preferencesDataStore(name = "manzil_secrets")

@Singleton
class KeystoreSecretVault @Inject constructor(
    @ApplicationContext private val context: Context
) : SecretVault {

    private object Keys {
        val API_KEY = stringPreferencesKey("openrouter_api_key_enc")
        val MODEL = stringPreferencesKey("openrouter_model")
    }

    override suspend fun saveApiKey(key: String) {
        val encrypted = encrypt(key.trim())
        context.secretDataStore.edit { prefs ->
            if (encrypted == null) prefs.remove(Keys.API_KEY) else prefs[Keys.API_KEY] = encrypted
        }
    }

    override suspend fun getApiKey(): String? {
        val stored = runCatching { context.secretDataStore.data.first()[Keys.API_KEY] }.getOrNull()
        if (stored.isNullOrBlank()) return null
        return decrypt(stored)
    }

    override suspend fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()

    override suspend fun clearApiKey() {
        context.secretDataStore.edit { it.remove(Keys.API_KEY) }
    }

    override suspend fun saveModel(model: String) {
        context.secretDataStore.edit { it[Keys.MODEL] = model }
    }

    override suspend fun getModel(): String? =
        runCatching { context.secretDataStore.data.first()[Keys.MODEL] }.getOrNull()

    /** `sk-or-v1-••••••••4f2a` for anything that gets shown on screen. */
    suspend fun maskedApiKey(): String? {
        val key = getApiKey() ?: return null
        val tail = key.takeLast(4)
        return "${key.take(9)}••••••••$tail"
    }

    private fun encrypt(plain: String): String? = runCatching {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val packed = iv + body
        Base64.encodeToString(packed, Base64.NO_WRAP)
    }.onFailure { AppLog.e(TAG, "encrypt failed", it) }.getOrNull()

    private fun decrypt(packed: String): String? = runCatching {
        val bytes = Base64.decode(packed, Base64.NO_WRAP)
        if (bytes.size < IV_SIZE) return null
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val body = bytes.copyOfRange(IV_SIZE, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
        String(cipher.doFinal(body), Charsets.UTF_8)
    }.onFailure { AppLog.e(TAG, "decrypt failed", it) }.getOrNull()

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val TAG = "SecretVault"
        const val ALIAS = "manzil_vault_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
    }
}
