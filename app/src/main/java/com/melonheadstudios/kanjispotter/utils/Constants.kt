package com.melonheadstudios.kanjispotter.utils

/**
 * kanjispotter
 * Created by jake on 2017-04-17, 7:36 PM
 */
class Constants {
    companion object {
        val SERVICE_STATUS_FLAG = "serviceStatus"
        val DARK_THEME_FLAG = "darkTheme"
        val BLACKLIST_SELECTION_STATUS_FLAG = "blacklistAllChecked"
        val PREFERENCES_KEY = "com.melonhead.android_quick_settings"

        val EVENT_USED = "parsed_kanji"
        val EVENT_ADDED_OPTION = "manually_selected_words"
        val EVENT_API = "checked_api"
        val EVENT_SWITCHED_WORDS = "switched_words_using_tabs"
        val EVENT_CLIPBOARD = "copied_to_clipboard"
        val ATTRIBUTE_WORDS = "words_per_use"
        val ATTRIBUTE_CHARACTERS = "characters_per_use"
    }
}