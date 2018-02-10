package com.melonheadstudios.kanjispotter.extensions

import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import java.lang.reflect.Type

/**
 * kanjispotter
 * Created by jake on 2018-02-09, 7:34 PM
 */
fun <T> Gson.from(string: String, type: Type): T? {
    return try {
        this.fromJson<T>(string, type)
    } catch (e: Exception) {
        Crashlytics.log(string)
        Crashlytics.logException(e)
        null
    }
}