package com.melonheadstudios.kanjispotter.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.services.PreferencesService
import com.melonheadstudios.kanjispotter.views.KanjiHoverDisplay
import org.koin.android.ext.android.inject

class KanjiBubbleActivity: AppCompatActivity() {
    private val kanjiRepo: KanjiRepo by inject()
    private val preferencesService: PreferencesService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
    }
}