package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.crashlytics.android.Crashlytics
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.managers.TextManager
import javax.inject.Inject

class JapaneseTextGrabberService : AccessibilityService() {
    private val tag = "JapaneseTextGrabber"

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var textManager: TextManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        MainApplication.graph.inject(this)
        Log.d(tag, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            event?.packageName ?: return
            val blackListEnabled = prefManager.blacklistEnabled()
            val parsingEnabled = prefManager.overlayEnabled()
            if (!parsingEnabled) return
            if (blackListEnabled) {
                if (prefManager.blacklisted(event.packageName)) return
            }
            textManager.parseEvent(event)
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
    }

    override fun onInterrupt() {
    }
}
