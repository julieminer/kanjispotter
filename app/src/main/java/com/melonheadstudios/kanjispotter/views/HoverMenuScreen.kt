package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.util.Log
import android.view.View
import com.atilika.kuromoji.ipadic.Tokenizer
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.managers.TextManager
import com.melonheadstudios.kanjispotter.models.*
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.melonheadstudios.kanjispotter.viewmodels.InfoPanelViewHolder
import com.squareup.otto.Subscribe
import io.mattcarroll.hover.Content
import javax.inject.Inject


/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:53 PM
 */

class HoverMenuScreen(val context: Context) : Content {


    val tag = HoverMenuScreen::class.java.simpleName

    @Inject
    lateinit var iabManager: IABManager

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var bus: MainThreadBus

    @Inject
    lateinit var tokenizer: Tokenizer

    @Inject
    lateinit var textManager: TextManager

    init {
        MainApplication.graph.inject(this)
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
        viewHolder = InfoPanelViewHolder(mContext, view, iabManager, bus, tokenizer)
        return view
    }

    override fun onShown() {
        if (BuildConfig.DEBUG) {
            textManager.handleEventText("食べる見る犬")
        }
    }

    override fun onHidden() {
        // no-op
    }

    @Subscribe
    fun onInfoClearEvent(e: InfoPanelClearEvent) {
        Log.d(tag, "clearPanel")
        viewHolder?.clearPanel()
    }

    @Subscribe
    fun onInfoPanelEvent(e: InfoPanelEvent) {
        Log.d(tag, "handleString: ${e.chosenWord} = ${e.json}")
        viewHolder?.updateView(e.chosenWord, e.json)
    }

    @Subscribe
    fun onErrorEvent(e: InfoPanelErrorEvent) {
        Log.d(tag, "handleError: ${e.errorText}")
        viewHolder?.handleError(e.errorText, e.showHeaders)
    }

    @Subscribe
    fun onSelectEvent(it: InfoPanelSelectionsEvent) {
        Log.d(tag, "handleSelections: ${it.selections}")
        viewHolder?.updateSelections(it.selections)
    }

    @Subscribe
    fun onMultiSelectEvent(it: InfoPanelMultiSelectEvent) {
        Log.d(tag, "handle multiselect event ${it.rawString}")
        viewHolder?.handleMultiSelectionEvent(it.rawString)
    }

    @Subscribe
    fun onSelectedWordEvent(it: InfoPanelSelectedWordEvent) {
        Log.d(tag, "selected position ${it.position}")
        viewHolder?.selectedPosition(it.position)
    }

    @Subscribe
    fun onPrefChangedEvent(it: InfoPanelPreferenceChanged) {
        if (!it.enabled) {
            viewHolder?.makeInvisibile(fromTile = true)
        }
    }

    @Subscribe
    fun onTokenizedEvent(e: TokenizedEvent) {
        Log.d(tag, "handle tokenized event")
        viewHolder?.handleToken(e.token)
    }
}