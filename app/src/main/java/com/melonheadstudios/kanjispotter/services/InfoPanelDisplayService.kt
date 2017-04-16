package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.support.constraint.ConstraintLayout
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
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.InfoPanelClearEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelEvent
import com.melonheadstudios.kanjispotter.models.JishoResponse
import com.melonheadstudios.kanjispotter.viewmodels.KanjiListModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.HeaderAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * GlobalActionBarService
 * Created by jake on 2017-04-15, 9:17 AM
 */
class InfoPanelDisplayService: AccessibilityService() {
    private val TAG = "InfoPanelDisplay"
    var mLayout: FrameLayout? = null
    var viewHolder: ViewHolder? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(TAG, "Service connected")
        // Create an overlay and display the action bar
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        val inflater = LayoutInflater.from(this)
        val parent = inflater.inflate(R.layout.action_bar, mLayout)
        viewHolder = ViewHolder(applicationContext, parent)
        wm.addView(mLayout, lp)

        Bus.observe<InfoPanelClearEvent>()
                .subscribe { clearPanel() }
                .registerInBus(this)

        Bus.observe<InfoPanelEvent>()
                .subscribe { handleString(it.chosenWord, it.json) }
                .registerInBus(this) //registers your subscription to unsubscribe it properly later
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    private fun handleString(chosenWord: String, string: String) {
        Log.d(TAG, "handleString: $chosenWord = $string")
        viewHolder?.updateView(chosenWord, string)
    }

    private fun clearPanel() {
        Log.d(TAG, "clearPanel")
        viewHolder?.clearPanel()
    }

    class ViewHolder(context: Context, parent: View) {
        val container: ConstraintLayout = parent.findViewById(R.id.info_panel) as ConstraintLayout
        val list: RecyclerView = parent.findViewById(R.id.info) as RecyclerView
        val chosenWord: TextView = parent.findViewById(R.id.info_word) as TextView
        val button: Button = parent.findViewById(R.id.info_button) as Button
//        val headerList: RecyclerView = parent.findViewById(R.id.info) as RecyclerView

        val fastAdapter = FastAdapter<KanjiListModel>()
        val itemAdapter = ItemAdapter<KanjiListModel>()
        var items = ArrayList<KanjiListModel>()

        init {
            button.setOnClickListener {
                makeInvisibile()
            }

            list.layoutManager = LinearLayoutManager(context)
            list.layoutManager.isAutoMeasureEnabled = true
            list.itemAnimator = DefaultItemAnimator()
            list.adapter = itemAdapter.wrap(fastAdapter)

//            itemAdapter.set(cachedSamples.getObject())
//            list.setAdapter(stickyHeaderAdapter.wrap(itemAdapter.wrap(headerAdapter.wrap(fastAdapter))))
//            list.setAdapter(itemAdapter.wrap(fastAdapter))
//            val decoration = StickyRecyclerHeadersDecoration(stickyHeaderAdapter)
        }

        fun updateView(word: String, string: String) {
            chosenWord.text =  word
            parseJsonString(string, word)
            makeVisible()
        }

        fun makeInvisibile() {
            clearPanel()
            container.visibility = GONE
        }

        fun clearPanel() {
            itemAdapter.clear()
            chosenWord.text = ""
        }

        private fun makeVisible() {
            container.visibility = VISIBLE
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

            items = ArrayList(LinkedHashSet(items));
            items.sortBy { it.kanjiText }
            itemAdapter.set(items)
        }
    }
}