package com.melonheadstudios.kanjispotter.utils

/**
 * kanjispotter
 * Created by jake on 2017-04-17, 7:36 PM
 */
class Constants {
    companion object {
        val SERVICE_STATUS_FLAG = "serviceStatus"
        val DARK_THEME_FLAG = "darkTheme"
        val BLACKLIST_STATUS_FLAG = "blacklistEnabled"
        val BLACKLIST_SELECTION_STATUS_FLAG = "blacklistAllChecked"
        val APP_BLACKLISTED = "appBlacklisted:"
        val PREFERENCES_KEY = "com.melonhead.android_quick_settings"

        val EVENT_USED = "Parsed Kanji"
        val EVENT_API = "Checked API"
        val EVENT_SWITCHED_WORDS = "Switched Words using tabs"
        val EVENT_CLIPBOARD = "Copied to clipboard"
        val ATTRIBUTE_WORDS = "Words per use"
        val ATTRIBUTE_CHARACTERS = "Characters per use"
    }
}