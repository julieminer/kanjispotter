package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.crashlytics.android.Crashlytics
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.managers.TextManager
import com.melonheadstudios.kanjispotter.models.InfoPanelAddOptionEvent
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.squareup.otto.Subscribe
import javax.inject.Inject


class JapaneseTextGrabberService : AccessibilityService() {
    private val tag = "JapaneseTextGrabber"

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var textManager: TextManager

    @Inject
    lateinit var bus: MainThreadBus

    override fun onServiceConnected() {
        super.onServiceConnected()

        MainApplication.graph.inject(this)
        bus.register(this)

        Log.d(tag, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            event?.packageName ?: return
            val blackListEnabled = prefManager.blacklistEnabled()
            if (blackListEnabled) {
                if (prefManager.blacklisted(event.packageName)) return
            }
            if (!isServiceRunning(InfoPanelDisplayService::class.java)) {
                val service = Intent(applicationContext, InfoPanelDisplayService::class.java)
                startService(service)
            }
            textManager.parseEvent(event)
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

    @Subscribe
    fun onAddOptionEvent(it: InfoPanelAddOptionEvent) {
        textManager.addSelectionOption(it.option)
    }
}
