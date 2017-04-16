package com.melonheadstudios.kanjispotter.viewmodels

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.JishoResponse
import com.melonheadstudios.kanjispotter.services.QuickTileService
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 2:09 PM
 */

class InfoPanelViewHolder(context: Context, parent: View) {
    private val TAG = "InfoPanelViewHolder"

    val container: CardView = parent.findViewById(R.id.info_panel) as CardView
    val list: RecyclerView = parent.findViewById(R.id.info) as RecyclerView
    val button: ImageButton = parent.findViewById(R.id.info_button) as ImageButton
    val headerList: RecyclerView = parent.findViewById(R.id.info_word) as RecyclerView

    val fastAdapter = FastAdapter<KanjiListModel>()
    val itemAdapter = ItemAdapter<KanjiListModel>()
    var items = ArrayList<KanjiListModel>()

    val headerFastAdapter = FastAdapter<KanjiSelectionListModel>()
    val headerItemAdapter = ItemAdapter<KanjiSelectionListModel>()
    var headerItems = ArrayList<KanjiSelectionListModel>()

    init {
        button.setOnClickListener {
            makeInvisibile()
        }

        list.layoutManager = LinearLayoutManager(context)
        list.layoutManager.isAutoMeasureEnabled = true
        list.itemAnimator = DefaultItemAnimator()
        list.adapter = itemAdapter.wrap(fastAdapter)

        headerList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        headerList.layoutManager.isAutoMeasureEnabled = true
        headerList.itemAnimator = DefaultItemAnimator()
        headerList.adapter = headerItemAdapter.wrap(headerFastAdapter)

        headerFastAdapter.withItemEvent(KanjiSelectionListModel.RadioButtonClickEvent())
    }

    fun updateView(word: String, string: String) {
        parseJsonString(string, word)
        makeVisible()
    }

    fun updateSelections(selections: List<String>) {
        selections.forEach {
            headerItems.add(KanjiSelectionListModel(it))
        }
        headerItems = ArrayList(LinkedHashSet(headerItems))
        headerItems.sortBy { it.selectedWord }
        headerItemAdapter.set(headerItems)
    }

    fun selectedPosition(position: Int) {
        val header = headerFastAdapter.getItem(position)
        itemAdapter.set(items.filter {
            Log.d(TAG, "Filtering word ${it.selectedWord} w/ ${header.selectedWord}")
            it.selectedWord == header.selectedWord
        })
    }

    fun makeInvisibile() {
        clearPanel()
        container.alpha = 1f
        container.scaleX = 1f
        container.scaleY = 1f
        container.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(300)
                .start()
    }

    fun clearPanel() {
        items.clear()
        itemAdapter.set(items)
        headerItems.clear()
        headerItemAdapter.set(headerItems)
    }

    private fun makeVisible() {
        val prefs = container.context.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean(QuickTileService.SERVICE_STATUS_FLAG, false)
        container.visibility = if (isActive) VISIBLE else GONE
        container.alpha = 0f
        container.scaleX = 0f
        container.scaleY = 0f
        container.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start()
    }

    private fun parseJsonString(string: String, selectedWord: String) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jishoResponse = mapper.readValue<JishoResponse>(string)

        val dataArray = jishoResponse.data ?: return
        for ((_, japanese) in dataArray) {
            val japaneseData = japanese ?: continue
            for ((reading1, word) in japaneseData) {
                val reading = reading1 ?: ""
                val wordData = word ?: ""

                if (wordData.isEmpty() || reading.isEmpty()) {
                    continue
                }

                items.add(KanjiListModel(wordData, reading, selectedWord))
            }
        }

        items = ArrayList(LinkedHashSet(items))
        items.sortBy { it.kanjiText }
        itemAdapter.set(items)

        if (headerItems.size > 0) {
            headerFastAdapter.select(0)
            selectedPosition(0)
        }
    }
}