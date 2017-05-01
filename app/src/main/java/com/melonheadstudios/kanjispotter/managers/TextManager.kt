package com.melonheadstudios.kanjispotter.managers

import android.view.accessibility.AccessibilityEvent
import com.eightbitlab.rxbus.Bus
import com.melonheadstudios.kanjispotter.extensions.getReadings
import com.melonheadstudios.kanjispotter.extensions.stringify
import com.melonheadstudios.kanjispotter.utils.JapaneseCharMatcher
import javax.inject.Singleton
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.melonheadstudios.kanjispotter.models.*
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.ATTRIBUTE_CHARACTERS
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.ATTRIBUTE_WORDS
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.EVENT_API
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.EVENT_USED


/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:57 AM
 */
@Singleton
class TextManager {
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

    private fun getEventText(event: AccessibilityEvent?): String {
        val sb = StringBuilder()
        val sbi = StringBuilder()
        event?.text ?: return ""

        for (s in event.text) {
            s.forEach { e ->
                if (JapaneseCharMatcher.isKanji(e)) {
                    sb.append(e)
                } else {
                    sb.append(" ")
                }
            }
        }

        val text = event.source?.text ?: return sb.stringify()
        for (s in text) {
            if (JapaneseCharMatcher.isKanji(s)) {
                sbi.append(s)
            } else {
                sbi.append(" ")
            }
        }

        val outer = sb.stringify()
        val inner = sbi.stringify()

        if (outer == inner) {
            return inner
        } else {
            return "outer = $outer inner = $inner"
        }
    }

    fun parseEvent(event: AccessibilityEvent) {
        if (!getEventType(event)) return
        val text = getEventText(event)
        if (text.isEmpty()) return

        Bus.send(InfoPanelClearEvent())
        val components = text.split(" ")
        if (components.size > 1) {
            Bus.send(InfoPanelMultiSelectEvent(text.trim()))
        }
        Bus.send(InfoPanelSelectionsEvent(components))
        Answers.getInstance().logCustom(CustomEvent(EVENT_USED)
                .putCustomAttribute(ATTRIBUTE_WORDS, components.size)
                .putCustomAttribute(ATTRIBUTE_CHARACTERS, text.length))
        components.forEach {
            it.getReadings { readings ->
                if (readings.isEmpty()) {
                    Bus.send(InfoPanelErrorEvent("No data to display. Are you connected to the internet?"))
                    return@getReadings
                }

                Answers.getInstance().logCustom(CustomEvent(EVENT_API))
                Bus.send(InfoPanelEvent(chosenWord = it, json = readings))
            }
        }
    }
}