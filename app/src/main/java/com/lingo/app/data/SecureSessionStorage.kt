package com.lingo.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.SettingsSessionManager

/**
 * Keeps the Supabase session (refresh token) in Android Keystore-backed
 * storage. Version 1.8 and earlier wrote it to the default preferences file;
 * the first encrypted launch copies the session across, then removes the old
 * copy. If the Keystore cannot be opened, the session is kept in memory only
 * rather than falling back to plaintext.
 */
@Suppress("DEPRECATION")
internal object SecureSessionStorage {
    private const val ENCRYPTED_FILE = "lingo_secure_auth"
    private const val LEGACY_FILE = "com.lingo.app_preferences"
    private const val SESSION_KEY = "session"

    fun create(context: Context): SessionManager {
        val encrypted = runCatching { encryptedSettings(context) }.getOrNull()
        if (encrypted == null) {
            // Do not leave a usable refresh token in plaintext if the Keystore
            // is unavailable. The learner signs in again instead.
            legacyPreferences(context).edit().clear().apply()
            return MemorySessionManager()
        }
        migrateLegacySession(context, encrypted)
        return SettingsSessionManager(encrypted)
    }

    private fun encryptedSettings(context: Context): Settings {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = runCatching {
            createEncryptedPreferences(context, masterKey)
        }.getOrElse {
            // A corrupt keyset or an interrupted first write is recoverable by
            // starting a fresh encrypted file. This signs the learner out.
            context.deleteSharedPreferences(ENCRYPTED_FILE)
            createEncryptedPreferences(context, masterKey)
        }
        return SharedPreferencesSettings(prefs)
    }

    private fun createEncryptedPreferences(context: Context, masterKey: MasterKey) =
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    private fun migrateLegacySession(context: Context, encrypted: Settings) {
        val legacy = legacyPreferences(context)
        // Supabase 3.x names the key after the project
        // ("sb-<ref>-supabase-co-session"); older builds used "session".
        val oldSession = legacy.all.entries
            .firstOrNull { (key, value) ->
                value is String && (key == SESSION_KEY || key.endsWith("-session"))
            }
            ?.value as? String
        if (oldSession != null && encrypted.getStringOrNull(SESSION_KEY) == null) {
            runCatching { encrypted.putString(SESSION_KEY, oldSession) }
        }
        // Clear the old file whether or not the copy succeeded. A failed copy
        // should require a fresh sign-in, not leave the token readable.
        legacy.edit().clear().apply()
    }

    private fun legacyPreferences(context: Context) =
        context.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE)
}
