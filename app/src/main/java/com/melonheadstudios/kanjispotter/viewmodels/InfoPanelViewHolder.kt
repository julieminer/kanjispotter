package com.melonheadstudios.kanjispotter.viewmodels

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.ProgressBar
import android.widget.TextView
import com.atilika.kuromoji.ipadic.Token
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.models.InfoPanelErrorEvent
import com.melonheadstudios.kanjispotter.models.JishoModel
import com.melonheadstudios.kanjispotter.models.englishDefinition
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 * kanjispotter
 * Created by jake on 2017-04-16, 2:09 PM
 */

class InfoPanelViewHolder(val context: Context,
                          parent: View,
                          iabManager: IABManager,
                          private val bus: MainThreadBus) {
    private val tag = "InfoPanelViewHolder"

    private val list: RecyclerView = parent.findViewById(R.id.info)
    private val headerList: RecyclerView = parent.findViewById(R.id.info_word)
    private val progressBar: ProgressBar = parent.findViewById(R.id.progress_bar)
    private val errorText: TextView = parent.findViewById(R.id.error_text)

    private val fastAdapter = FastAdapter<KanjiListModel>()
    private val itemAdapter = ItemAdapter<KanjiListModel>()
    private var items = ArrayList<KanjiListModel>()

    private val headerFastAdapter = FastAdapter<KanjiSelectionListModel>()
    private val headerItemAdapter = ItemAdapter<KanjiSelectionListModel>()
    private var headerItems = ArrayList<KanjiSelectionListModel>()

    init {
        list.layoutManager = LinearLayoutManager(context)
        list.itemAnimator = DefaultItemAnimator()
        list.adapter = itemAdapter.wrap(fastAdapter)

        headerList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        headerList.itemAnimator = DefaultItemAnimator()
        headerList.adapter = headerItemAdapter.wrap(headerFastAdapter)

        headerFastAdapter.withItemEvent(KanjiSelectionListModel.RadioButtonClickEvent())

        iabManager.setupIAB(context)
    }

    fun handleError(error: String, showHeader: Boolean) {
        headerList.visibility = if (showHeader) VISIBLE else INVISIBLE
        list.visibility = INVISIBLE
        progressBar.visibility = GONE
        errorText.visibility = VISIBLE
        errorText.text = error
    }

    fun updateSelections(selections: List<String>) {
        selections.forEach {
            val item = KanjiSelectionListModel(it, bus)
            if (!headerItems.contains(item)) {
                headerItems.add(item)
                headerItems = ArrayList(LinkedHashSet(headerItems))
                headerItems.sortBy { it.selectedWord }
                headerItemAdapter.set(headerItems)
            }
        }
    }

    @Suppress("UselessCallOnCollection")
    fun selectedPosition(position: Int) {
        val header = headerFastAdapter.getItem(position) ?: return
        itemAdapter.set(items.filterNotNull().filter {
            Log.d(tag, "Filtering word ${it.selectedWord} w/ ${header.selectedWord}")
            it.selectedWord == header.selectedWord
        })
    }

    private fun clearError() {
        errorText.visibility = GONE
        errorText.text = ""
    }

    private fun hideProgress() {
        progressBar.visibility = GONE
        headerList.visibility = VISIBLE
        list.visibility = VISIBLE
    }

    private fun parseToken(token: Token, jishoModel: JishoModel?) = async(UI) {
        items.add(KanjiListModel(token.baseForm, token.reading, token.baseForm, jishoModel?.englishDefinition()))

        items = ArrayList(LinkedHashSet(items))
        items.sortBy { it.kanjiText }
        itemAdapter.set(items)

        if (items.isEmpty()) {
            bus.post(InfoPanelErrorEvent(errorText = "No data for this selection", showHeaders = true))
        }

        headerFastAdapter.deselect()
        headerFastAdapter.select(0)
        selectedPosition(0)
    }

    fun handleToken(token: Token, jishoModel: JishoModel?) {
        clearError()
        hideProgress()
        parseToken(token, jishoModel)
    }
}