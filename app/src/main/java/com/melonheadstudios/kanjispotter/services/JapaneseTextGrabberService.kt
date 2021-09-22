package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.extensions.shouldParse
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JapaneseTextGrabberService : AccessibilityService(), KoinComponent {
    private val tag = "JapaneseTextGrabber"
    private val kanjiRepo: KanjiRepo by inject()
    private val preferencesService: PreferencesService by inject()

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
                val blackList = preferencesService.blackListedApps.firstOrNull() ?: setOf()
                if (preferencesService.overlayEnabled.firstOrNull() != true) return@runBlocking
                if (blackList.contains(event.packageName)) return@runBlocking
                    kanjiRepo.parse(AccessibilityEventHolder(event.packageName.toString(), event.text.toString()))
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onInterrupt() {
    }
}

data class AccessibilityEventHolder(val packageName: String, val text: String)