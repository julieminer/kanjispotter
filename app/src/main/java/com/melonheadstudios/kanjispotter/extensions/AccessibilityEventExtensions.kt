package com.melonheadstudios.kanjispotter.extensions

import android.view.accessibility.AccessibilityEvent

/**
 * kanjispotter
 * Created by jake on 2018-05-28, 8:08 PM
 */
fun AccessibilityEvent.shouldParse(): Boolean {
    when (eventType) {
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