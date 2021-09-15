package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.extensions.shouldParse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class JapaneseTextGrabberService : AccessibilityService() {
    private val tag = "JapaneseTextGrabber"
    private val app: MainApplication by lazy { MainApplication.instance }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            event?.packageName ?: return
            event.text ?: return
            if (!event.shouldParse()) return
            runBlocking {
                if (app.dataStore.overlayEnabled.firstOrNull() != true) return@runBlocking
//                if (app.dataStore.blackListEnabled.firstOrNull() == true)
                MainApplication.instance.kanjiRepo.parse(AccessibilityEventHolder(event.packageName.toString(), event.text.toString()))
            }
//            if (MainApplication.instance.prefManager.blacklistEnabled() &&
//                    MainApplication.instance.prefManager.blacklisted(event.packageName)) {
//                return
//            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onInterrupt() {
    }
}

data class AccessibilityEventHolder(val packageName: String, val text: String)