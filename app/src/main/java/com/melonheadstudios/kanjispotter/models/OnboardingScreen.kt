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
            Accessibility -> ""
            Bubbles -> ""
        }

    val actionTitle: String
        get() = when (this) {
            Accessibility -> "Enable Accessibility Service"
            Bubbles -> "Enable Notification Bubbles"
        }
}