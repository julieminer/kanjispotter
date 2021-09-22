package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.models.Kanji
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.services.PreferencesService
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun Home(preferencesService: PreferencesService = get(), kanjiRepo: KanjiRepo = get()) {
    val overlayEnabled = preferencesService.overlayEnabled.collectAsState(initial = false)
    val darkThemeEnabled = preferencesService.darkThemeEnabled.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val exampleKanji = remember { mutableStateOf(setOf<Kanji>()) }

    LaunchedEffect(true) {
        exampleKanji.value = kanjiRepo.kanjiForText(text = "食べる\n男の人\nご主人")
    }

    Column(modifier = Modifier.fillMaxHeight()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 10.dp)) {
            Text(text = "Appearance", style = MaterialTheme.typography.h5)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(text = "Kanji Spotter Enabled",
                        style = MaterialTheme.typography.subtitle2)
                    //Text(text = "", style = MaterialTheme.typography.caption)
                }
                Switch(checked = overlayEnabled.value == true, onCheckedChange = { coroutineScope.launch { preferencesService.setOverlayEnabled(it) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.primaryVariant
                    )
                )
            }
            if (overlayEnabled.value == true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "Dark Theme Enabled",
                            style = MaterialTheme.typography.subtitle2)
                        // Text(text = "", style = MaterialTheme.typography.caption)
                    }
                    Switch(checked = darkThemeEnabled.value == true, onCheckedChange = { coroutineScope.launch { preferencesService.setDarkThemeEnabled(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colors.primary,
                            checkedTrackColor = MaterialTheme.colors.primaryVariant
                        )
                    )
                }
                Text(text = "Preview", style = MaterialTheme.typography.h5, modifier = Modifier.padding(top = 15.dp))
            }
        }
        if (overlayEnabled.value == true) {
            Card(Modifier.padding(5.dp), elevation = 10.dp) {
                KanjiHoverDisplay(
                    modifier = Modifier.fillMaxHeight(0.75f),
                    parsedKanji = exampleKanji.value,
                    filteredKanji = setOf(),
                    showAllClicked = { },
                    onFilterToggled = { }
                )
            }
        }
    }
}

@Preview
@Composable
fun HomePreviewDark() {
    MaterialTheme(colors = darkColors()) {
        Home()
    }
}

@Preview
@Composable
fun HomePreviewLight() {
    MaterialTheme(colors = lightColors()) {
        Home()
    }
}
