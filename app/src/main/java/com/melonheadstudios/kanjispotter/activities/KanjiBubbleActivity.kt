package com.melonheadstudios.kanjispotter.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.views.KanjiHoverDisplay
import org.koin.android.ext.android.inject

class KanjiBubbleActivity: AppCompatActivity() {
    private val kanjiRepo: KanjiRepo by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContent {
            MaterialTheme {
                val parsedKanji = kanjiRepo.parsedKanji.collectAsState(initial = setOf())
                val filteredKanji = kanjiRepo.filteredKanji.collectAsState(initial = setOf())
                KanjiHoverDisplay(
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