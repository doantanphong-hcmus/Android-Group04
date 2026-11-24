package com.tangkinhcac.app.data.local.auth

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores only encrypted token bytes; the AES key itself never leaves Android Keystore. */
class KeystoreAuthTokenStore(
    context: Context,
) : AuthTokenStore {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override val token: String?
        get() {
            val encodedCiphertext = preferences.getString(KEY_CIPHERTEXT, null) ?: return null
            val encodedIv = preferences.getString(KEY_IV, null) ?: return null

            return try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(encodedIv, Base64.NO_WRAP)),
                )
                String(
                    cipher.doFinal(Base64.decode(encodedCiphertext, Base64.NO_WRAP)),
                    StandardCharsets.UTF_8,
                )
            } catch (_: GeneralSecurityException) {
                clearUnreadableToken()
                null
            } catch (_: IOException) {
                clearUnreadableToken()
                null
            } catch (_: IllegalArgumentException) {
                clearUnreadableToken()
                null
            }
        }

    @SuppressLint("ApplySharedPref", "UseKtx")
    override fun save(token: String): Boolean {
        if (token.isBlank()) {
            return false
        }

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val ciphertext = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))

            // Commit synchronously because navigation must not outrun persistence of the new session.
            preferences
                .edit()
                .putString(KEY_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .commit()
        } catch (_: GeneralSecurityException) {
            false
        } catch (_: IOException) {
            false
        }
    }

    override fun clear() {
        preferences.edit { clear() }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore =
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply {
                init(
                    KeyGenParameterSpec
                        .Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(AES_KEY_SIZE_BITS)
                        .build(),
                )
            }.generateKey()
    }

    private fun clearUnreadableToken() {
        // A restored or invalidated ciphertext is unusable without its device-bound key.
        preferences.edit { clear() }
    }

    private companion object {
        const val PREFERENCES_NAME = "auth_credentials"
        const val KEY_CIPHERTEXT = "access_token_ciphertext"
        const val KEY_IV = "access_token_iv"
        const val KEY_ALIAS = "tang_kinh_cac_auth_token"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val AES_KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
