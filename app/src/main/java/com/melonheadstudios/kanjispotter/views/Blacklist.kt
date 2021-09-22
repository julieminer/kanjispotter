package com.melonheadstudios.kanjispotter.views

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.melonheadstudios.kanjispotter.extensions.verticalFadingEdge
import com.melonheadstudios.kanjispotter.models.BlacklistApp
import com.melonheadstudios.kanjispotter.services.PreferencesService
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

// TODO: 2021-09-17 add sorting options + ui
@Composable
fun Blacklist(preferencesService: PreferencesService = get()) {
    val blackListedPackages = preferencesService.blackListedApps.collectAsState(initial = setOf())
    val scope = rememberCoroutineScope()
    val blacklistApps = remember { mutableStateOf(setOf<BlacklistApp>()) }
    val context = LocalContext.current

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

    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize()) {
        Text(text = "Blacklist Apps", style = MaterialTheme.typography.h5)
        Text(text = "Prevent Kanji Spotter from triggering from certain apps", style = MaterialTheme.typography.body2)
        val state = rememberLazyListState()
        val arrangement = Arrangement.spacedBy(15.dp)
        LazyColumn(state = state,
            verticalArrangement = arrangement,
            modifier = Modifier
                .padding(vertical = 15.dp)
                .verticalFadingEdge(state, length = 50.dp, verticalArrangement = arrangement)) {
            items(blacklistApps.value.toList()) { app ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                            bitmap = app.icon.toBitmap().asImageBitmap(),
                            contentDescription = app.packageName,
                            modifier = Modifier
                                    .size(50.dp)
                    )
                    Column(modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .weight(1f)) {
                        Text(text = app.name, style = MaterialTheme.typography.subtitle2)
                        Text(text = app.packageName, style = MaterialTheme.typography.caption)
                    }
                    Switch(
                        colors = colors(
                            checkedThumbColor = MaterialTheme.colors.primary,
                            checkedTrackColor = MaterialTheme.colors.primaryVariant
                        ),
                        checked = blackListedPackages.value?.contains(app.packageName) == true,
                        onCheckedChange = { checked ->
                            val newList = blackListedPackages.value?.toMutableSet() ?: mutableSetOf()
                            if (checked) {
                                newList.add(app.packageName)
                            } else {
                                newList.remove(app.packageName)
                            }
                            scope.launch {
                                preferencesService.setBlackListApps(newList)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun PreviewBlackList() {
    MaterialTheme {
        Blacklist()
    }
}