package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.support.constraint.ConstraintLayout
import android.text.method.ScrollingMovementMethod
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
        viewHolder = ViewHolder(parent)
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

    class ViewHolder(parent: View) {
        val container: ConstraintLayout = parent.findViewById(R.id.info_panel) as ConstraintLayout
        val text: TextView = parent.findViewById(R.id.info) as TextView
        val chosenWord: TextView = parent.findViewById(R.id.info_word) as TextView
        val button: Button = parent.findViewById(R.id.info_button) as Button

        init {
            button.setOnClickListener {
                makeInvisibile()
            }
            text.movementMethod = ScrollingMovementMethod()
        }

        fun updateView(word: String, string: String) {
            chosenWord.text =  word
            parseJsonString(string)
            makeVisible()
        }

        fun makeInvisibile() {
            container.visibility = GONE
            text.text = ""
        }

        fun clearPanel() {
            text.text = ""
            chosenWord.text = ""
        }

        private fun makeVisible() {
            container.visibility = VISIBLE
        }

        private fun parseJsonString(string: String) {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            val jishoResponse = mapper.readValue<JishoResponse>(string)

            val dataArray = jishoResponse.data ?: return

            var totalText = ""
            for (jishoResponseData in dataArray) {
                val japaneseData = jishoResponseData.japanese ?: continue
                for (japaneseResponse in japaneseData) {
                    val reading = japaneseResponse.reading ?: ""
                    val wordData = japaneseResponse.word ?: ""
                    // if there is only the reading
                    // otherwise
                    if (wordData.isEmpty() || reading.isEmpty()) {
                        continue
                    }

                    totalText += "$wordData --- $reading"
                    totalText += "\n"
                }
            }

            text.text = (text.text?.toString() ?: "") + totalText
        }
    }
}