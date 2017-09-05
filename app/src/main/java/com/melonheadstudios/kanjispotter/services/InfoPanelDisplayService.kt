package com.melonheadstudios.kanjispotter.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.*
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.melonheadstudios.kanjispotter.viewmodels.InfoPanelViewHolder
import com.squareup.otto.Subscribe
import javax.inject.Inject

/**
 * GlobalActionBarService
 * Created by jake on 2017-04-15, 9:17 AM
 */
class InfoPanelDisplayService: Service() {
    private val TAG = "InfoPanelDisplay"

    @Inject
    lateinit var iabManager: IABManager

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var bus: MainThreadBus

    private var mLayout: FrameLayout? = null
    private var viewHolder: InfoPanelViewHolder? = null
    private var windowManager: WindowManager? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Suppress("DEPRECATION")
    override fun onCreate() {
        MainApplication.graph.inject(this)

        updateTheme()
        super.onCreate()

        bus.register(this)

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
        viewHolder = InfoPanelViewHolder(applicationContext, parent, iabManager, bus)
        try {
            windowManager?.addView(mLayout, params)
        } catch (e: Exception) {
            Log.e(TAG, "", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewHolder?.destroy()
        if (mLayout != null) windowManager?.removeView(mLayout)
        bus.unregister(this)
    }

    private fun updateTheme() {
        if (prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppTheme)
        } else {
            setTheme(R.style.AppThemeLight)
        }
    }

    @Subscribe
    fun onInfoClearEvent(e: InfoPanelClearEvent) {
        Log.d(TAG, "clearPanel")
        viewHolder?.clearPanel()
    }

    @Subscribe
    fun onInfoPanelEvent(e: InfoPanelEvent) {
        Log.d(TAG, "handleString: ${e.chosenWord} = ${e.json}")
        viewHolder?.updateView(e.chosenWord, e.json)
    }

    @Subscribe
    fun onErrorEvent(e: InfoPanelErrorEvent) {
        Log.d(TAG, "handleError: ${e.errorText}")
        viewHolder?.handleError(e.errorText, e.showHeaders)
    }

    @Subscribe
    fun onSelectEvent(it: InfoPanelSelectionsEvent) {
        Log.d(TAG, "handleSelections: ${it.selections}")
        viewHolder?.updateSelections(it.selections)
    }

    @Subscribe
    fun onMultiSelectEvent(it: InfoPanelMultiSelectEvent) {
        Log.d(TAG, "handle multiselect event ${it.rawString}")
        viewHolder?.handleMultiSelectionEvent(it.rawString)
    }

    @Subscribe
    fun onSelectedWordEvent(it: InfoPanelSelectedWordEvent) {
        Log.d(TAG, "selected position ${it.position}")
        viewHolder?.selectedPosition(it.position)
    }

    @Subscribe
    fun onPrefChangedEvent(it: InfoPanelPreferenceChanged) {
        if (!it.enabled) {
            viewHolder?.makeInvisibile(fromTile = true)
        }
    }
}