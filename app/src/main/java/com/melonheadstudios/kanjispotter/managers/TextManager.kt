package com.melonheadstudios.kanjispotter.managers

import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import com.eightbitlab.rxbus.Bus
import com.melonheadstudios.kanjispotter.extensions.getReadings
import com.melonheadstudios.kanjispotter.extensions.stringify
import com.melonheadstudios.kanjispotter.models.InfoPanelClearEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelSelectionsEvent
import com.melonheadstudios.kanjispotter.utils.JapaneseCharMatcher
import javax.inject.Singleton

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
        event ?: return ""

        for (s in event.text) {
            s.forEach { e ->
                if (JapaneseCharMatcher.isKanji(e)) {
                    sb.append(e)
                } else {
                    sb.append(" ")
                }
            }
        }

        val text = event.source.text ?: return sb.stringify()
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

        Handler().postDelayed({
            val components = text.split(" ")
            Bus.send(InfoPanelSelectionsEvent(components))
            components.forEach {
                it.getReadings { readings ->
                    Bus.send(InfoPanelEvent(chosenWord = it, json = readings))
                }
            }
        }, 250)

    }
}