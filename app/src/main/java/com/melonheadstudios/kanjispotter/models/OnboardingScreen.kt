package com.melonheadstudios.kanjispotter.models

import com.melonheadstudios.kanjispotter.R

sealed class OnboardingScreen {
    object Accessibility: OnboardingScreen()
    object Bubbles: OnboardingScreen()

    val title: String
        get() = when (this) {
            Accessibility -> "Accessibility"
            Bubbles -> "Bubbles"
        }

    val imageId: Int
        get() = when (this) {
            Accessibility -> R.drawable.accessibility_settings
            Bubbles -> R.drawable.bubble_settings
        }

    val imageContentDescription: String
        get() = when (this) {
            Accessibility -> "Accessibility Permission Screenshot"
            Bubbles -> "Notification Bubbles Settings Screenshot"
        }

    val description: String
        get() = when (this) {
            Accessibility -> "In order to read text in other apps, you'll need to enable Kanji Spotter as an Accessibility Service"
            Bubbles -> "In order to display over other apps, you'll need to enable Bubbling for all Notifications from Kanji Spotter"
        }

    val actionTitle: String
        get() = when (this) {
            Accessibility -> "Enable Accessibility Service"
            Bubbles -> "Enable Notification Bubbles"
        }
}