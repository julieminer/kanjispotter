package com.melonheadstudios.kanjispotter.managers

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.melonheadstudios.kanjispotter.utils.Constants
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.DARK_THEME_FLAG
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import javax.inject.Singleton

/**
 * kanjispotter
 * Created by jake on 2017-04-21, 11:34 PM
 */
@SuppressLint("ApplySharedPref")
@Singleton
class PrefManager(appContext: Context) {
    val prefs: SharedPreferences = appContext.getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE)

    fun darkThemeEnabled(): Boolean {
        return prefs.getBoolean(Constants.DARK_THEME_FLAG, true)
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(DARK_THEME_FLAG, enabled).commit()
    }

    fun overlayEnabled(): Boolean {
        return prefs.getBoolean(Constants.SERVICE_STATUS_FLAG, true)
    }

    fun blacklistEnabled(): Boolean {
        return prefs.getBoolean(Constants.BLACKLIST_STATUS_FLAG, false)
    }

    fun blacklisted(packageName: CharSequence): Boolean {
        return prefs.getBoolean(Constants.APP_BLACKLISTED + packageName, false)
    }

    fun setBlacklist(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.BLACKLIST_STATUS_FLAG, enabled).apply()
    }

    fun allBlackListChecked(): Boolean {
        return prefs.getBoolean(Constants.BLACKLIST_SELECTION_STATUS_FLAG, false)
    }

    fun setAllBlackListChecked(selectedAll: Boolean) {
        val edit = prefs.edit()
        edit.putBoolean(Constants.BLACKLIST_SELECTION_STATUS_FLAG, selectedAll)
        edit.commit()
    }

    fun setAllAppsBlackilist(selectedAll: Boolean, items: ArrayList<BlacklistSelectionModel>) {
        val edit = prefs.edit()
        edit.putBoolean(Constants.BLACKLIST_SELECTION_STATUS_FLAG, selectedAll)
        for (item in items) {
            edit.putBoolean(Constants.APP_BLACKLISTED + item.packageName, selectedAll)
        }
        edit.commit()
    }

    @SuppressLint("CommitPrefEdits")
    fun setOverlay(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.SERVICE_STATUS_FLAG, enabled).commit()
    }

    fun serviceStatus(): Boolean {
        return prefs.getBoolean(Constants.SERVICE_STATUS_FLAG, true)
    }

    fun serviceStatus(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.SERVICE_STATUS_FLAG, enabled).commit()
    }
}
