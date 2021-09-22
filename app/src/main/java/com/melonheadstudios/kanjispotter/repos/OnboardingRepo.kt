package com.melonheadstudios.kanjispotter.repos

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.models.OnboardingScreen
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.utils.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class OnboardingRepo(private val notificationManager: NotificationManager, private val appContext: Context, appScope: CoroutineScope) {
    private val mutableCanBubble = MutableStateFlow(false)
    private val mutableAccessibilityServiceRunning = MutableStateFlow(false)
    private val mutableShowOnboarding = MutableStateFlow(true)

    val canBubble = mutableCanBubble.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)
    val accessibilityServiceRunning = mutableAccessibilityServiceRunning.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)

    init {
        appScope.launch {
            checkPermissions()
        }
    }

    fun checkPermissions() {
        mutableCanBubble.value = notificationManager.canBubble()
        mutableAccessibilityServiceRunning.value = appContext.isServiceRunning(JapaneseTextGrabberService::class.java)
        mutableShowOnboarding.value = !(mutableCanBubble.value && mutableAccessibilityServiceRunning.value)
    }

    fun startAction(context: Context, screen: OnboardingScreen) {
        when (screen) {
            OnboardingScreen.Accessibility -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
            OnboardingScreen.Bubbles -> {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
                )
            }
        }
    }
}