package com.melonheadstudios.kanjispotter.views

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
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
import com.melonheadstudios.kanjispotter.utils.DataStore
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Home : NavigationItem("home", R.drawable.close_button, "Home")
    object Settings : NavigationItem("setting", R.drawable.close_light, "Settings")
    object History : NavigationItem("history", R.drawable.close_button, "History")
    object Blacklist : NavigationItem("blacklist", R.drawable.close_button, "Blacklist")

    companion object {
        val allValues = listOf(Home, History, Blacklist, Settings)
    }
}

@Composable
fun BottomNav(navController: NavController) {
    BottomNavigation(
        backgroundColor = colorResource(id = R.color.colorPrimary),
    ) {
        NavigationItem.allValues.forEach { item ->
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White.copy(0.4f),
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
fun BlackListScreen(navController: NavController, dataStore: DataStore = get()) {
    val blackListedPackages = dataStore.blackListedApps.collectAsState(initial = setOf())
    val scope = rememberCoroutineScope()

    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    val context = LocalContext.current
    val packageManager = context.packageManager
    val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
    val blackListApps = remember {
        pkgAppsList.mapNotNull {
            val appLabel = it.loadLabel(packageManager).toString()
            val packageName = it.activityInfo.taskAffinity ?: return@mapNotNull null
            val packageIcon = it.loadIcon(packageManager)
            BlacklistApp(name = appLabel, packageName = packageName, icon = packageIcon)
        }
    }

    Blacklist(blacklistApps = blackListApps.toSet(),
        blacklistedPackages = blackListedPackages.value ?: setOf(),
        blackListValueToggled = { packageName, isBlackListed ->
            val newList = blackListedPackages.value?.toMutableSet() ?: mutableSetOf()
            if (isBlackListed) {
                newList.add(packageName)
            } else {
                newList.remove(packageName)
            }
            scope.launch {
                dataStore.setBlackListApps(newList)
            }
    })
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNav(navController) }) { innerPadding ->
        NavHost(navController, startDestination = NavigationItem.Blacklist.route, modifier = Modifier.padding(innerPadding)) {
//            composable(NavigationItem.Home.route) { Profile(navController) }
            composable(NavigationItem.Blacklist.route) { BlackListScreen(navController) }
//            composable(NavigationItem.Settings.route) { FriendsList(navController) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun HomeScreenPreview() {
    MainScreen()
}