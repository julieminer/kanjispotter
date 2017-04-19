package com.melonheadstudios.kanjispotter.viewmodels

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.eightbitlab.rxbus.Bus
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.models.InfoPanelErrorEvent
import com.melonheadstudios.kanjispotter.models.JishoResponse
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.PREFERENCES_KEY
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.SERVICE_STATUS_FLAG
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 2:09 PM
 */

class InfoPanelViewHolder(val context: Context, parent: View, var iabManager: IABManager) {
    private val TAG = "InfoPanelViewHolder"

    val adView: AdView = parent.findViewById(R.id.ad_spot) as AdView
    val container: CardView = parent.findViewById(R.id.info_panel) as CardView
    val list: RecyclerView = parent.findViewById(R.id.info) as RecyclerView
    val button: ImageButton = parent.findViewById(R.id.info_button) as ImageButton
    val headerList: RecyclerView = parent.findViewById(R.id.info_word) as RecyclerView
    val progressBar: ProgressBar = parent.findViewById(R.id.progress_bar) as ProgressBar
    val errorText: TextView = parent.findViewById(R.id.error_text) as TextView

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

        updateAd()

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

    fun clearPanel() {
        showProgress()
        makeVisible()
        items.clear()
        itemAdapter.set(items)
        headerItems.clear()
        headerItemAdapter.set(headerItems)
    }

    fun updateAd(isPremium: Boolean = false) {
        val shouldShowAds = !BuildConfig.DEBUG && !isPremium
        if (shouldShowAds) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = GONE
            val parent = adView.parent as ViewGroup? ?: return
            parent.removeView(adView)
        }
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

        if (headerItems.size > 0) {
            headerFastAdapter.select(0)
            selectedPosition(0)
        }
    }
}