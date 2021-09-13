package com.melonheadstudios.kanjispotter.viewmodels

import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.KanjiInstance
import com.melonheadstudios.kanjispotter.services.JishoService
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter


/**
 * kanjispotter
 * Created by jake on 2017-04-16, 2:09 PM
 */

class InfoPanelViewHolder(val context: Context,
                          parent: View,
                          private val bus: MainThreadBus,
                          private val jishoService: JishoService) {
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
        list.layoutManager =
            LinearLayoutManager(context)
        list.itemAnimator = DefaultItemAnimator()
        list.adapter = itemAdapter.wrap(fastAdapter)

        headerList.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        headerList.itemAnimator =
            DefaultItemAnimator()
        headerList.adapter = headerItemAdapter.wrap(headerFastAdapter)

        headerFastAdapter.withItemEvent(KanjiSelectionListModel.RadioButtonClickEvent())
    }

    private fun updateSelections(selections: List<String>) {
        selections.forEach {
            val item = KanjiSelectionListModel(it, bus)
            if (!headerItems.contains(item)) {
                headerItems.add(item)
                headerItemAdapter.set(headerItems)
            }
        }
    }

    @Suppress("UselessCallOnCollection")
    fun selectedPosition(position: Int) {
        val header = headerFastAdapter.getItem(position) ?: return
        itemAdapter.set(items.filterNotNull().filter {
            Log.d(tag, "Filtering word ${it.kanjiText} w/ ${header.selectedWord}")
            it.kanjiText == header.selectedWord
        })
    }

    fun displayKanji(kanji: List<KanjiInstance>) {
        updateSelections(kanji.map { it.token.baseForm })
        kanji.forEach {
            items.add(KanjiListModel(it, jishoService))
        }
        itemAdapter.set(items)
        headerFastAdapter.deselect()
        headerFastAdapter.select(0)
        selectedPosition(0)
    }
}