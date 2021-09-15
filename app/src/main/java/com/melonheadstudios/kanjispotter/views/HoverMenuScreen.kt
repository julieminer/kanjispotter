package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.view.View
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import io.mattcarroll.hover.Content

/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:53 PM
 */

class HoverMenuScreen(private val kanjiRepo: KanjiRepo, context: Context) : Content {
    private val mContext: Context = context.applicationContext

    override fun getView(): View {
        return createScreenView()
    }

    override fun isFullscreen(): Boolean {
        return false
    }

    private fun createScreenView(): View {
        val view = View.inflate(mContext, R.layout.spotter_content, null) as ComposeView
        view.setContent {
            MaterialTheme {
                val parsedKanji = kanjiRepo.parsedKanji.collectAsState(initial = setOf())
                val filteredKanji = kanjiRepo.filteredKanji.collectAsState(initial = setOf())
                KanjiHoverDisplay(
                        parsedKanji = parsedKanji.value,
                        filteredKanji = filteredKanji.value,
                        onFilterToggled = { kanji ->
                            kanjiRepo.toggleFilter(kanji)
                        }
                )
            }
        }
        return view
    }

    override fun onShown() {
        // no-op
    }

    override fun onHidden() {
        // no-op
    }
}