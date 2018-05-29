package com.melonheadstudios.kanjispotter.viewmodels

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.models.KanjiInstance
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter


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

    private fun updateSelections(selections: List<String>) {
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

    fun displayKanji(kanji: List<KanjiInstance>) {
        updateSelections(kanji.map { it.token.baseForm })
        kanji.forEach {
            items.add(KanjiListModel(it.token.baseForm, it.token.reading, it.token.baseForm))
        }
        itemAdapter.set(items)
        headerFastAdapter.deselect()
        headerFastAdapter.select(0)
        selectedPosition(0)
    }
}