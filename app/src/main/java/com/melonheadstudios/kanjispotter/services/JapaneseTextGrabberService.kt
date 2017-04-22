package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.managers.TextManager
import javax.inject.Inject


class JapaneseTextGrabberService : AccessibilityService() {
    private val TAG = "JapaneseTextGrabber"

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var textManager: TextManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        Log.d(TAG, "Service connected")

        val info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info

        val service = Intent(applicationContext, InfoPanelDisplayService::class.java)
        if (isMyServiceRunning(InfoPanelDisplayService::class.java)) {
            stopService(service)
        }
        startService(service)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val blackListEnabled = prefManager.blacklistEnabled()
        if (blackListEnabled) {
            if (prefManager.blacklisted(event.packageName)) return
        }
        if (!isMyServiceRunning(InfoPanelDisplayService::class.java)) {
            val service = Intent(applicationContext, InfoPanelDisplayService::class.java)
            startService(service)
        }
        textManager.parseEvent(event)
    }

    override fun onInterrupt() {
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }

}
