package com.melonheadstudios.kanjispotter.extensions

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.TypedValue

@Suppress("DEPRECATION") // getRunningServices only returns this applications services as of time of deprecation. this is all we need
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
    return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
}

