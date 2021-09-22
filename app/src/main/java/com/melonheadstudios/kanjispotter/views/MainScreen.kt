package com.melonheadstudios.kanjispotter.views

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.BlacklistApp
import com.melonheadstudios.kanjispotter.models.Kanji
import com.melonheadstudios.kanjispotter.models.OnboardingScreen
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.repos.OnboardingRepo
import com.melonheadstudios.kanjispotter.services.PreferencesService
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

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

@Composable
fun BottomNav(navController: NavController) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        NavigationItem.bottomNavItems.forEach { item ->
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon!!), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selectedContentColor = MaterialTheme.colors.onSurface,
                unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
                alwaysShowLabel = true,
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                })
        }
    }
}

@Composable
fun HomeScreen(preferencesService: PreferencesService = get(), kanjiRepo: KanjiRepo = get()) {
    val enabled = preferencesService.overlayEnabled.collectAsState(initial = false)
    val darkTheme = preferencesService.darkThemeEnabled.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val exampleKanji = remember { mutableStateOf(setOf<Kanji>()) }

    LaunchedEffect(true) {
        exampleKanji.value = kanjiRepo.kanjiForText(text = "食べる\n男の人\nご主人")
    }

    Home(
        exampleKanji = exampleKanji.value,
        overlayEnabled = enabled.value == true,
        darkThemeEnabled = darkTheme.value == true,
        onOverlayToggled = { coroutineScope.launch { preferencesService.setOverlayEnabled(it) } },
        darkThemeToggled = { coroutineScope.launch { preferencesService.setDarkThemeEnabled(it) } },
    )
}

@Composable
fun BlackListScreen(preferencesService: PreferencesService = get()) {
    val blackListedPackages = preferencesService.blackListedApps.collectAsState(initial = setOf())
    val scope = rememberCoroutineScope()
    val blacklistApps = remember { mutableStateOf(setOf<BlacklistApp>()) }
    val context = LocalContext.current

    if (blacklistApps.value.isEmpty()) {
        LaunchedEffect(true) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val packageManager = context.packageManager
            val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
            blacklistApps.value = pkgAppsList.mapNotNull {
                val appLabel = it.loadLabel(packageManager).toString()
                val packageName = it.activityInfo.taskAffinity ?: return@mapNotNull null
                val packageIcon = it.loadIcon(packageManager)
                BlacklistApp(name = appLabel, packageName = packageName, icon = packageIcon)
            }.toSet()
        }
    }

    Blacklist(blacklistApps = blacklistApps.value,
        blacklistedPackages = blackListedPackages.value ?: setOf(),
        blackListValueToggled = { packageName, isBlackListed ->
            val newList = blackListedPackages.value?.toMutableSet() ?: mutableSetOf()
            if (isBlackListed) {
                newList.add(packageName)
            } else {
                newList.remove(packageName)
            }
            scope.launch {
                preferencesService.setBlackListApps(newList)
            }
    })
}

@Composable
fun HistoryScreen() {

}

@Composable
fun SettingsScreen() {

}

@Composable
fun OnboardingComposer(onboardingScreen: OnboardingScreen, onboardingRepo: OnboardingRepo = get()) {
    val context = LocalContext.current
    Onboarding(
        title = onboardingScreen.title,
        imageId = onboardingScreen.imageId,
        imageContentDescription = onboardingScreen.imageContentDescription,
        description = onboardingScreen.description,
        actionTitle = onboardingScreen.actionTitle,
        onContinueTapped = {
            onboardingRepo.startAction(context, OnboardingScreen.Accessibility)
        }
    )
}

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
                composable(NavigationItem.Home.route) { HomeScreen() }
                composable(NavigationItem.Blacklist.route) { BlackListScreen() }
                composable(NavigationItem.History.route) { HistoryScreen() }
                composable(NavigationItem.Settings.route) { SettingsScreen() }
                composable(NavigationItem.Settings.route) { SettingsScreen() }
                composable(NavigationItem.OnboardingAccessibility.route) { OnboardingComposer(OnboardingScreen.Accessibility) }
                composable(NavigationItem.OnboardingBubbles.route) { OnboardingComposer(OnboardingScreen.Bubbles) }
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