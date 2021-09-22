package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.services.PreferencesService
import org.koin.androidx.compose.get

@Composable
fun HoverScreen(preferencesService: PreferencesService = get(), kanjiRepo: KanjiRepo = get()) {
    val darkThemeEnabled = preferencesService.darkThemeEnabled.collectAsState(initial = false)
    MaterialTheme(colors = if (darkThemeEnabled.value == true) darkColors() else lightColors()) {
        val parsedKanji = kanjiRepo.parsedKanji.collectAsState(initial = setOf())
        val filteredKanji = kanjiRepo.filteredKanji.collectAsState(initial = setOf())
        KanjiHoverDisplay(
            modifier = Modifier.fillMaxSize(),
            parsedKanji = parsedKanji.value,
            filteredKanji = filteredKanji.value,
            showAllClicked = { kanjiRepo.toggleAllClicked() },
            onFilterToggled = { kanji ->
                kanjiRepo.toggleFilter(kanji)
            }
        )
    }
}