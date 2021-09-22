package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.melonheadstudios.kanjispotter.models.NavigationItem
import com.melonheadstudios.kanjispotter.models.OnboardingScreen
import com.melonheadstudios.kanjispotter.repos.OnboardingRepo
import com.melonheadstudios.kanjispotter.services.PreferencesService
import org.koin.androidx.compose.get

@Composable
fun MainScreen(preferencesService: PreferencesService = get(), onboardingRepo: OnboardingRepo = get()) {
    val navController = rememberNavController()
    val darkThemeEnabled = preferencesService.darkThemeEnabled.collectAsState(initial = false)
    val canBubble = onboardingRepo.canBubble.collectAsState(initial = false)
    val accessibilityRunning = onboardingRepo.accessibilityServiceRunning.collectAsState(initial = false)

    val startDest = when {
        !accessibilityRunning.value -> NavigationItem.OnboardingAccessibility
        !canBubble.value -> NavigationItem.OnboardingBubbles
        else -> NavigationItem.Home
    }

    MaterialTheme(colors = if (darkThemeEnabled.value == true) darkColors() else lightColors()) {
        Scaffold(bottomBar = {
            if (canBubble.value && accessibilityRunning.value) {
                BottomNav(navController)
            }
        }) { innerPadding ->
            NavHost(
                navController,
                startDestination = startDest.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavigationItem.Home.route) { Home() }
                composable(NavigationItem.Blacklist.route) { Blacklist() }
                composable(NavigationItem.History.route) { History() }
                composable(NavigationItem.Settings.route) { Settings() }
                composable(NavigationItem.OnboardingAccessibility.route) { Onboarding(OnboardingScreen.Accessibility) }
                composable(NavigationItem.OnboardingBubbles.route) { Onboarding(OnboardingScreen.Bubbles) }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    Surface {
        MainScreen()
    }
}