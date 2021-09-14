package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.view.View
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.viewmodels.InfoPanelViewHolder
import io.mattcarroll.hover.Content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:53 PM
 */

class HoverMenuScreen(private val kanjiRepo: KanjiRepo, private val scope: CoroutineScope, context: Context) : Content {
    private val tag: String = HoverMenuScreen::class.java.simpleName

    private val mContext: Context = context.applicationContext
    private var viewHolder: InfoPanelViewHolder? = null

    override fun getView(): View {
        return createScreenView()
    }

    override fun isFullscreen(): Boolean {
        return false
    }

    private fun createScreenView(): View {
        val view = View.inflate(mContext, R.layout.spotter_content, null)
        viewHolder = InfoPanelViewHolder(mContext, scope, view) { position ->
            kanjiRepo.select(position)
        }
        scope.launch(Dispatchers.Main) {
            kanjiRepo.parsedKanji.collect {
                viewHolder?.displayKanji(it)
            }
        }
        scope.launch {
            kanjiRepo.selectedKanjiPosition.collect {
                viewHolder?.selectedPosition(it)
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