package com.melonheadstudios.kanjispotter.managers

import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.atilika.kuromoji.ipadic.Tokenizer
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.models.InfoPanelSelectionsEvent
import com.melonheadstudios.kanjispotter.models.JishoModel
import com.melonheadstudios.kanjispotter.models.TokenizedEvent
import com.melonheadstudios.kanjispotter.services.HoverPanelService
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.EVENT_API
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:57 AM
 */
@Singleton
class TextManager(private val applicationContext: Context) {
    @Inject
    lateinit var bus: MainThreadBus

    @Inject
    lateinit var tokenizer: Tokenizer

    @Inject
    lateinit var moshi: Moshi

    init {
        MainApplication.graph.inject(this)
    }

    private fun getEventType(event: AccessibilityEvent): Boolean {
        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> return false
            AccessibilityEvent.TYPE_VIEW_CLICKED -> return true
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> return true
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> return true
            AccessibilityEvent.TYPE_VIEW_SELECTED -> return true
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> return false
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> return false
        }
        return false
    }

    fun handleEventText(text: String) = async(UI) {
        val tokens = tokenizer.tokenize(text) ?: return@async
        val knownTokens = tokens.filter { it.isKnown }
        bus.post(InfoPanelSelectionsEvent(knownTokens.map { it.baseForm }))

        if (knownTokens.isNotEmpty() && !applicationContext.isServiceRunning(HoverPanelService::class.java)) {
            val startHoverIntent = Intent(applicationContext, HoverPanelService::class.java)
//            startHoverIntent.putExtra("reading", readings)
            applicationContext.startService(startHoverIntent)
        }

        knownTokens.forEach {
            bus.post(TokenizedEvent(token = it, jishoModel = getJishoModel(it.baseForm)))
        }

        Answers.getInstance().logCustom(CustomEvent(EVENT_API))

    }

    private suspend fun getJishoModel(text: String): JishoModel? = suspendCoroutine { continuation ->
        val dir = "http://jisho.org/api/v1/search/words?keyword="
        (dir + text).httpGet().responseString { _, _, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    continuation.resume(null)
                }
                is Result.Success -> {
                    try {
                        val data = result.get()
                        val jsonAdapter = moshi.adapter(JishoModel::class.java)
                        val response = jsonAdapter.fromJson(data)
                        continuation.resume(response)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Crashlytics.logException(e)
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    fun parseEvent(event: AccessibilityEvent?) {
        event ?: return
        if (!getEventType(event)) return
        handleEventText(event.text.toString())
    }
}