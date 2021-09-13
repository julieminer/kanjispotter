package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.extensions.shouldParse
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import javax.inject.Inject

class JapaneseTextGrabberService : AccessibilityService() {
    private val tag = "JapaneseTextGrabber"

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var kanjiRepo: KanjiRepo

    override fun onServiceConnected() {
        super.onServiceConnected()

        MainApplication.graph.inject(this)
        Log.d(tag, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            event?.packageName ?: return
            event.text ?: return
            if (!event.shouldParse()) return
            if (!prefManager.overlayEnabled()) return
            if (prefManager.blacklistEnabled() &&
                    prefManager.blacklisted(event.packageName)) {
                return
            }
            kanjiRepo.parse(AccessibilityEventHolder(event.packageName.toString(), event.text.toString()))
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onInterrupt() {
    }
}

data class AccessibilityEventHolder(val packageName: String, val text: String)