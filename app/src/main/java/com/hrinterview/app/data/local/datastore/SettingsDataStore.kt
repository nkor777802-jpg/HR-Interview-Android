package com.hrinterview.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hrinterview.app.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("hr_settings")

class SettingsDataStore(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val agreementKey = booleanPreferencesKey("agreement_accepted")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[themeKey] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    val agreementAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[agreementKey] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun acceptAgreement() {
        context.dataStore.edit { it[agreementKey] = true }
    }
}
