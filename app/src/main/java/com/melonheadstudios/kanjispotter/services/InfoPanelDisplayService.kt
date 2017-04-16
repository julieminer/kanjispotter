package com.melonheadstudios.kanjispotter.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.*
import com.melonheadstudios.kanjispotter.services.QuickTileService.Companion.SERVICE_STATUS_FLAG
import com.melonheadstudios.kanjispotter.viewmodels.KanjiListModel
import com.melonheadstudios.kanjispotter.viewmodels.KanjiSelectionListModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * GlobalActionBarService
 * Created by jake on 2017-04-15, 9:17 AM
 */
class InfoPanelDisplayService: Service() {
    val TAG = "InfoPanelDisplay"

    var mLayout: FrameLayout? = null
    var viewHolder: ViewHolder? = null
    var windowManager: WindowManager? = null
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Service created")
        // Create an overlay and display the action bar
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        val inflater = LayoutInflater.from(this)
        val parent = inflater.inflate(R.layout.action_bar, mLayout)
        viewHolder = ViewHolder(applicationContext, parent)
        try {
            windowManager?.addView(mLayout, params)
        } catch (e: Exception) {
            Log.e(TAG, "", e)
        }

        Bus.observe<InfoPanelClearEvent>()
                .subscribe { clearPanel() }
                .registerInBus(this)

        Bus.observe<InfoPanelEvent>()
                .subscribe { handleString(it.chosenWord, it.json) }
                .registerInBus(this) //registers your subscription to unsubscribe it properly later

        Bus.observe<InfoPanelSelectionsEvent>()
                .subscribe { handleSelections(it.selections) }
                .registerInBus(this)

        Bus.observe<InfoPanelSelectedWordEvent>()
                .subscribe { selectedPosition(it.position) }
                .registerInBus(this)

        Bus.observe<InfoPanelDisabledEvent>()
                .subscribe { viewHolder?.makeInvisibile() }
                .registerInBus(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLayout != null) windowManager?.removeView(mLayout)
    }

    private fun handleString(chosenWord: String, string: String) {
        Log.d(TAG, "handleString: $chosenWord = $string")
        viewHolder?.updateView(chosenWord, string)
    }

    private fun handleSelections(selections: List<String>) {
        Log.d(TAG, "handleSelections: $selections")
        viewHolder?.updateSelections(selections)
    }

    private fun clearPanel() {
        Log.d(TAG, "clearPanel")
        viewHolder?.clearPanel()
    }

    private fun selectedPosition(position: Int) {
        Log.d(TAG, "selected position $position")
        viewHolder?.selectedPosition(position)
    }

    class ViewHolder(context: Context, parent: View) {
        private val TAG = "InfoPanelDisplay"

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
            container.visibility = GONE
        }

        fun clearPanel() {
            items.clear()
            itemAdapter.set(items)
            headerItems.clear()
            headerItemAdapter.set(headerItems)
        }

        private fun makeVisible() {
            val prefs = container.context.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
            val isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, false)
            container.visibility = if (isActive) VISIBLE else GONE

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
}