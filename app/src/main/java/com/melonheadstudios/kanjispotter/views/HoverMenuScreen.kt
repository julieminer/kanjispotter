package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.util.Log
import android.view.View
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.InfoPanelSelectedWordEvent
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.melonheadstudios.kanjispotter.viewmodels.InfoPanelViewHolder
import com.squareup.moshi.Moshi
import com.squareup.otto.Subscribe
import io.mattcarroll.hover.Content

/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:53 PM
 */

class HoverMenuScreen(val context: Context, val bus: MainThreadBus, val kanjiRepo: KanjiRepo, val moshi: Moshi) : Content {
    private val tag: String = HoverMenuScreen::class.java.simpleName

    init {
        bus.register(this)
    }

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
        viewHolder = InfoPanelViewHolder(mContext, view, bus, moshi)
        return view
    }

    override fun onShown() {
        viewHolder?.displayKanji(kanjiRepo.allKanji())
    }

    override fun onHidden() {
        // no-op
    }

    @Subscribe
    fun onSelectedWordEvent(it: InfoPanelSelectedWordEvent) {
        Log.d(tag, "selected position ${it.position}")
        viewHolder?.selectedPosition(it.position)
    }
}