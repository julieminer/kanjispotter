package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.TextManager
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.APP_BLACKLISTED
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.BLACKLIST_STATUS_FLAG
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.PREFERENCES_KEY
import javax.inject.Inject


class JapaneseTextGrabberService : AccessibilityService() {
    private val TAG = "JapaneseTextGrabber"
    private var prefs: SharedPreferences? = null

    @Inject
    lateinit var textManager: TextManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

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
        val blackListEnabled = prefs?.getBoolean(BLACKLIST_STATUS_FLAG, false) ?: false
        if (blackListEnabled) {
            val appBlacklisted = prefs?.getBoolean(APP_BLACKLISTED + event.packageName, false) ?: false
            if (appBlacklisted) return
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
