package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.support.annotation.NonNull
import android.view.Gravity
import android.view.View
import android.widget.TextView
import io.mattcarroll.hover.Content


/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:53 PM
 */

class HoverMenuScreen(context: Context, private val mPageTitle: String) : Content {

    private val mContext: Context = context.applicationContext

    override fun getView(): View {
        return createScreenView()
    }

    override fun isFullscreen(): Boolean {
        return true
    }

    private fun createScreenView(): View {
        val wholeScreen = TextView(mContext)
        wholeScreen.text = "Screen: $mPageTitle"
        wholeScreen.gravity = Gravity.CENTER
        return wholeScreen
    }

    override fun onShown() {
        // No-op
    }

    override fun onHidden() {
        // no-op
    }
}