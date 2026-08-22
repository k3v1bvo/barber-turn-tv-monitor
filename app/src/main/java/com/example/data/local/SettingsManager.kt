package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.data.model.SupabaseSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "barber_settings")

class SettingsManager(private val context: Context) {

    companion object {
        val KEY_SUPABASE_URL = stringPreferencesKey("supabase_url")
        val KEY_SUPABASE_KEY = stringPreferencesKey("supabase_key")
        val KEY_SHOP_NAME = stringPreferencesKey("shop_name")
        val KEY_IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val KEY_REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        val KEY_ROTATION_OFFSET = intPreferencesKey("rotation_offset")

        val DEFAULT_BUILD_URL: String get() {
            val nextUrl = try { BuildConfig.NEXT_PUBLIC_SUPABASE_URL } catch (_: Throwable) { "" }
            return nextUrl.ifBlank {
                BuildConfig.DEFAULT_SUPABASE_URL.ifBlank { "https://yqzvhtkakmnphoudsadg.supabase.co" }
            }
        }
        val DEFAULT_BUILD_KEY: String get() {
            val nextKey = try { BuildConfig.NEXT_PUBLIC_SUPABASE_ANON_KEY } catch (_: Throwable) { "" }
            return nextKey.ifBlank {
                BuildConfig.DEFAULT_SUPABASE_KEY
            }
        }
        val DEFAULT_BUILD_SHOP: String get() = BuildConfig.DEFAULT_SHOP_NAME.ifBlank { "BarberSite - Control de Turnos" }
        val DEFAULT_BUILD_IS_DEMO: Boolean get() = DEFAULT_BUILD_KEY.isBlank() || DEFAULT_BUILD_URL.contains("your-supabase-project")
    }

    val settingsFlow: Flow<SupabaseSettings> = context.dataStore.data.map { prefs ->
        val savedUrl = prefs[KEY_SUPABASE_URL]
        val savedKey = prefs[KEY_SUPABASE_KEY]
        val savedShop = prefs[KEY_SHOP_NAME]
        val savedDemo = prefs[KEY_IS_DEMO_MODE]

        val url = savedUrl ?: DEFAULT_BUILD_URL
        val apiKey = savedKey ?: DEFAULT_BUILD_KEY
        val shopName = savedShop ?: DEFAULT_BUILD_SHOP
        val isDemoMode = savedDemo ?: (apiKey.isBlank() || url.contains("your-supabase-project"))

        SupabaseSettings(
            url = url,
            apiKey = apiKey,
            shopName = shopName,
            isDemoMode = isDemoMode,
            refreshIntervalSec = prefs[KEY_REFRESH_INTERVAL] ?: 5
        )
    }

    val rotationOffsetFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROTATION_OFFSET] ?: 0
    }

    suspend fun saveSettings(url: String, key: String, shopName: String, isDemoMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SUPABASE_URL] = url.trim().removeSuffix("/")
            prefs[KEY_SUPABASE_KEY] = key.trim()
            prefs[KEY_SHOP_NAME] = shopName.ifBlank { DEFAULT_BUILD_SHOP }
            prefs[KEY_IS_DEMO_MODE] = isDemoMode
        }
    }

    suspend fun resetToDefaultSettings() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SUPABASE_URL] = DEFAULT_BUILD_URL
            prefs[KEY_SUPABASE_KEY] = DEFAULT_BUILD_KEY
            prefs[KEY_SHOP_NAME] = DEFAULT_BUILD_SHOP
            prefs[KEY_IS_DEMO_MODE] = DEFAULT_BUILD_IS_DEMO
            prefs[KEY_ROTATION_OFFSET] = 0
        }
    }

    suspend fun saveRotationOffset(offset: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROTATION_OFFSET] = offset
        }
    }

    suspend fun resetRotationOffset() {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROTATION_OFFSET] = 0
        }
    }
}
