package com.melonheadstudios.kanjispotter.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.melonheadstudios.kanjispotter.utils.Constants
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_KEY)

class DataStore(private val appContext: Context) {
    private val darkThemeKey = booleanPreferencesKey(Constants.DARK_THEME_FLAG)
    val darkThemeEnabled = appContext.dataStore.data.map { preferences -> preferences[darkThemeKey]  }

    private val overlayKey = booleanPreferencesKey(Constants.SERVICE_STATUS_FLAG)
    val overlayEnabled = appContext.dataStore.data.map { preferences -> preferences[overlayKey]  }

    private val blackListKey = booleanPreferencesKey(Constants.BLACKLIST_STATUS_FLAG)
    val blackListEnabled = appContext.dataStore.data.map { preferences -> preferences[blackListKey]  }

    private val blackListAppsKey = stringSetPreferencesKey(Constants.BLACKLIST_SELECTION_STATUS_FLAG)
    val blackListedApps = appContext.dataStore.data.map { preferences -> preferences[blackListAppsKey]  }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        appContext.dataStore.edit { settings -> settings[darkThemeKey] = enabled }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        appContext.dataStore.edit { settings -> settings[overlayKey] = enabled }
    }

    suspend fun setBlackListEnabled(enabled: Boolean) {
        appContext.dataStore.edit { settings -> settings[blackListKey] = enabled }
    }

    suspend fun setBlackListApps(apps: Set<String>) {
        appContext.dataStore.edit { settings -> settings[blackListAppsKey] = apps }
    }
}