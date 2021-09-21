package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.models.Kanji

@Composable
fun Home(exampleKanji: Set<Kanji>,
         overlayEnabled: Boolean,
         darkThemeEnabled: Boolean,
         onOverlayToggled: (Boolean) -> Unit,
         darkThemeToggled: (Boolean) -> Unit) {
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
                Switch(checked = overlayEnabled, onCheckedChange = onOverlayToggled)
            }
            if (overlayEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "Dark Theme Enabled",
                            style = MaterialTheme.typography.subtitle2)
                        // Text(text = "", style = MaterialTheme.typography.caption)
                    }
                    Switch(checked = darkThemeEnabled, onCheckedChange = darkThemeToggled)
                }
                Text(text = "Preview", style = MaterialTheme.typography.h5, modifier = Modifier.padding(top = 15.dp))
            }
        }
        if (overlayEnabled) {
            Card(Modifier.padding(5.dp), elevation = 10.dp) {
                KanjiHoverDisplay(
                    modifier = Modifier.fillMaxHeight(0.75f),
                    parsedKanji = exampleKanji,
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
    Home(exampleKanji = setOf(), overlayEnabled = true, darkThemeEnabled = true, onOverlayToggled = {}, darkThemeToggled = {})
}

@Preview
@Composable
fun HomePreviewLight() {
    Home(exampleKanji = setOf(), overlayEnabled = true, darkThemeEnabled = false, onOverlayToggled = {}, darkThemeToggled = {})
}
