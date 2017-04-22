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
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.*
import com.melonheadstudios.kanjispotter.viewmodels.InfoPanelViewHolder
import javax.inject.Inject

/**
 * GlobalActionBarService
 * Created by jake on 2017-04-15, 9:17 AM
 */
class InfoPanelDisplayService: Service() {
    val TAG = "InfoPanelDisplay"

    @Inject
    lateinit var iabManager: IABManager

    @Inject
    lateinit var prefManager: PrefManager

    var mLayout: FrameLayout? = null
    var viewHolder: InfoPanelViewHolder? = null
    var windowManager: WindowManager? = null
    var isPremium = false
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        updateTheme()
        super.onCreate()

        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

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
        viewHolder = InfoPanelViewHolder(applicationContext, parent, iabManager)
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
                .registerInBus(this)

        Bus.observe<InfoPanelErrorEvent>()
                .subscribe { handleError(it.errorText, it.showHeaders) }
                .registerInBus(this)

        Bus.observe<InfoPanelSelectionsEvent>()
                .subscribe { handleSelections(it.selections) }
                .registerInBus(this)

        Bus.observe<InfoPanelSelectedWordEvent>()
                .subscribe { selectedPosition(it.position) }
                .registerInBus(this)

        Bus.observe<IABUpdateUIEvent>()
                .subscribe {
                    isPremium = it.isPremium
                    viewHolder?.updateAd(isPremium)
                }
                .registerInBus(this)

        Bus.observe<InfoPanelPreferenceChanged>()
                .subscribe {
                    if (!it.enabled) {
                        viewHolder?.makeInvisibile(fromTile = true)
                    }
                }
                .registerInBus(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewHolder?.destroy()
        if (mLayout != null) windowManager?.removeView(mLayout)
    }

    private fun handleString(chosenWord: String, string: String) {
        Log.d(TAG, "handleString: $chosenWord = $string")
        viewHolder?.updateView(chosenWord, string)
    }

    private fun handleError(errorString: String, showHeaders: Boolean) {
        Log.d(TAG, "handleError: $errorString")
        viewHolder?.handleError(errorString, showHeaders)
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

    private fun updateTheme() {
        if (prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppTheme)
        } else {
            setTheme(R.style.AppThemeLight)
        }
    }
}