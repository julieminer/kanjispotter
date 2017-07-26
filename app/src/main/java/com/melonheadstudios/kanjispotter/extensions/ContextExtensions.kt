package com.melonheadstudios.kanjispotter.extensions

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.TypedValue

/**
 * kanjispotter
 * Created by jake on 2017-04-21, 9:51 PM
 */
fun Context.canDrawOverlays(): Boolean {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return Settings.canDrawOverlays(this)
    }
    return true
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
    return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
}

fun Context.pixels(forDP: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, forDP, resources.displayMetrics)
