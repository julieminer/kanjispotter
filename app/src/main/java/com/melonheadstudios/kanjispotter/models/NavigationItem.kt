package com.melonheadstudios.kanjispotter.models

import com.melonheadstudios.kanjispotter.R

sealed class NavigationItem(var route: String, var icon: Int?, var title: String) {
    object Home : NavigationItem("home", R.drawable.ic_baseline_home_24, "Home")
    object Settings : NavigationItem("setting", R.drawable.ic_baseline_settings_24, "Settings")
    object History : NavigationItem("history", R.drawable.ic_baseline_history_24, "History")
    object Blacklist : NavigationItem("blacklist", R.drawable.ic_baseline_filter_list_24, "Blacklist")
    object OnboardingAccessibility : NavigationItem("onboarding-accessibility}", null, "Accessibility Onboarding")
    object OnboardingBubbles : NavigationItem("onboarding-bubbles}", null, "Bubbles Onboarding")

    companion object {
        private const val devMode = false
        val bottomNavItems = if (devMode) listOf(Home, History, Blacklist, Settings) else listOf(Home, Blacklist)
    }
}
