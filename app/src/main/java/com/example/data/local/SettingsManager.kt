package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    }

    val settingsFlow: Flow<SupabaseSettings> = context.dataStore.data.map { prefs ->
        SupabaseSettings(
            url = prefs[KEY_SUPABASE_URL] ?: "https://your-supabase-project.supabase.co",
            apiKey = prefs[KEY_SUPABASE_KEY] ?: "",
            shopName = prefs[KEY_SHOP_NAME] ?: "BarberSite - Control de Turnos",
            isDemoMode = prefs[KEY_IS_DEMO_MODE] ?: true,
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
            prefs[KEY_SHOP_NAME] = shopName.ifBlank { "BarberSite - Control de Turnos" }
            prefs[KEY_IS_DEMO_MODE] = isDemoMode
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
