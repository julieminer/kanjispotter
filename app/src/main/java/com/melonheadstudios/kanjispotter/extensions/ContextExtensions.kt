package com.melonheadstudios.kanjispotter.extensions

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.melonheadstudios.kanjispotter.utils.Constants

@Suppress("DEPRECATION") // getRunningServices only returns this applications services as of time of deprecation. this is all we need
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
    return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
}

fun Context.saveToClipboard(text: String) {
    val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("KanjiSpotter", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(this, "Copied $text to clipboard", Toast.LENGTH_SHORT).show()
    FirebaseAnalytics.getInstance(this).logEvent(Constants.EVENT_CLIPBOARD, Bundle())
}
