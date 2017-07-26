package com.melonheadstudios.kanjispotter.viewmodels

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.eightbitlab.rxbus.Bus
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.models.InfoPanelAddOptionEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelErrorEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelSelectionsEvent
import com.melonheadstudios.kanjispotter.models.JishoResponse
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.PREFERENCES_KEY
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.SERVICE_STATUS_FLAG
import com.melonheadstudios.kanjispotter.views.NoTouchHorizontalScrollView
import com.melonheadstudios.kanjispotter.views.SelectionView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter


/**
 * kanjispotter
 * Created by jake on 2017-04-16, 2:09 PM
 */

class InfoPanelViewHolder(val context: Context, parent: View, var iabManager: IABManager) : SelectionView.SelectionViewDelegate {
    override fun selectedSegment(segment: String) {
        Log.d(TAG, segment)
        Bus.send(InfoPanelAddOptionEvent(segment))
        Bus.send(InfoPanelSelectionsEvent(listOf(segment)))
    }

    private val TAG = "InfoPanelViewHolder"

    val selectionScroller: SeekBar = parent.findViewById<SeekBar>(R.id.selection_scroll)
    val selectionView: SelectionView = parent.findViewById<SelectionView>(R.id.selection_view_text)
    val selectionViewContainer: NoTouchHorizontalScrollView = parent.findViewById<NoTouchHorizontalScrollView>(R.id.selection_view)
    val container: CardView = parent.findViewById<CardView>(R.id.info_panel)
    val list: RecyclerView = parent.findViewById<RecyclerView>(R.id.info)
    val button: ImageButton = parent.findViewById<ImageButton>(R.id.info_button)
    val headerList: RecyclerView = parent.findViewById<RecyclerView>(R.id.info_word)
    val progressBar: ProgressBar = parent.findViewById<ProgressBar>(R.id.progress_bar)
    val errorText: TextView = parent.findViewById<TextView>(R.id.error_text)

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

        selectionScroller.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val width = (selectionView.measuredWidth.toDouble() * progress.toDouble()) / 100.0
                selectionViewContainer.scrollTo(width.toInt(), 0)
            }
        })

        selectionView.delegate = this

        list.layoutManager = LinearLayoutManager(context)
        list.layoutManager.isAutoMeasureEnabled = true
        list.itemAnimator = DefaultItemAnimator()
        list.adapter = itemAdapter.wrap(fastAdapter)

        headerList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        headerList.layoutManager.isAutoMeasureEnabled = true
        headerList.itemAnimator = DefaultItemAnimator()
        headerList.adapter = headerItemAdapter.wrap(headerFastAdapter)

        headerFastAdapter.withItemEvent(KanjiSelectionListModel.RadioButtonClickEvent())

        iabManager.setupIAB(context)
    }

    fun destroy() {
        iabManager.unregister(context)
    }

    fun updateView(word: String, string: String) {
        clearError()
        hideProgress()
        parseJsonString(string, word)
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
            val item = KanjiSelectionListModel(it)
            if (!headerItems.contains(item)) {
                headerItems.add(item)
                headerItems = ArrayList(LinkedHashSet(headerItems))
                headerItems.sortBy { it.selectedWord }
                headerItemAdapter.set(headerItems)
            }
        }
    }

    fun selectedPosition(position: Int) {
        val header = headerFastAdapter.getItem(position) ?: return
        itemAdapter.set(items.filterNotNull().filter {
            Log.d(TAG, "Filtering word ${it.selectedWord} w/ ${header.selectedWord}")
            it.selectedWord == header.selectedWord
        })
    }

    fun handleMultiSelectionEvent(rawString: String) {
        val selectionList = ArrayList<TextSelection>()
        for (s in rawString) {
            selectionList.add(TextSelection(s.toString()))
        }
        selectionViewContainer.visibility = if (selectionList.count() <= 1) GONE else VISIBLE
        selectionView.viewTreeObserver.addOnGlobalLayoutListener {
            val hasManySelections = selectionList.count() > 0
            val needsScroll = selectionView.measuredWidth >= selectionScroller.measuredWidth
            val hasItems = selectionList.count() > 1
            selectionScroller.visibility = if (hasItems && hasManySelections && needsScroll) VISIBLE else GONE
        }
        selectionView.selectionsList = selectionList
    }

    fun clearPanel() {
        showProgress()
        makeVisible()
        items.clear()
        itemAdapter.set(items)
        headerItems.clear()
        headerItemAdapter.set(headerItems)
    }

    private fun clearError() {
        errorText.visibility = GONE
        errorText.text = ""
    }

    private fun showProgress() {
        progressBar.visibility = VISIBLE
        headerList.visibility = INVISIBLE
        list.visibility = INVISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = GONE
        headerList.visibility = VISIBLE
        list.visibility = VISIBLE
    }

    private fun makeVisible() {
        val prefs = container.context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, true)
        container.visibility = if (isActive) VISIBLE else GONE
        animateVisibility(from = 0f, to = 1f)
    }

    fun makeInvisibile(fromTile: Boolean = false) {
        clearPanel()
        if (!fromTile) {
            animateVisibility(from = 1f, to = 0f)
        } else {
            container.visibility = GONE
        }

    }

    private fun animateVisibility(from: Float, to: Float) {
        if (container.alpha == to) return

        container.alpha = from
        container.scaleX = from
        container.scaleY = from
        container.animate()
                .alpha(to)
                .scaleX(to)
                .scaleY(to)
                .setDuration(300)
                .withEndAction { container.visibility = if (to == 0f) GONE else container.visibility }
                .start()
    }

    private fun parseJsonString(string: String, selectedWord: String) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jishoResponse = mapper.readValue<JishoResponse>(string)

        val dataArray = jishoResponse.data ?: return
        for ((_, japanese, senses) in dataArray) {

            val japaneseData = japanese ?: continue
            val sensesData = senses ?: continue

            for ((english_definitions) in sensesData) {
                val definition = english_definitions?.joinToString(", ")
                for ((reading1, word) in japaneseData) {
                    val reading = reading1 ?: ""
                    val wordData = word ?: ""

                    if (wordData.isEmpty() || reading.isEmpty()) {
                        continue
                    }

                    items.add(KanjiListModel(wordData, reading, selectedWord, definition))
                }
            }
        }

        items = ArrayList(LinkedHashSet(items))
        items.sortBy { it.kanjiText }
        itemAdapter.set(items)

        if (items.isEmpty()) {
            Bus.send(InfoPanelErrorEvent(errorText = "No data for this selection", showHeaders = true))
        }

        headerFastAdapter.deselect()
        headerFastAdapter.select(0)
        selectedPosition(0)
    }
}