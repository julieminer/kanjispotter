package com.melonheadstudios.kanjispotter.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.eightbitlab.rxbus.Bus
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.InfoPanelDisabledEvent
import java.util.*

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 12:22 AM
 */
@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
class QuickTileService: TileService() {

    companion object {
        val SERVICE_STATUS_FLAG = "serviceStatus"
        val PREFERENCES_KEY = "com.melonhead.android_quick_settings"
    }

    override fun onClick() {
        updateTile()
    }

    private fun updateTile() {
        val tile = this.qsTile
        val isActive = toggleServiceStatus()

        val newIcon: Icon
        val newLabel: String
        val newState: Int

        // Change the tile to match the service status.
        if (isActive) {
            newLabel = String.format(Locale.US, "%s %s", getString(R.string.app_name), getString(R.string.service_active))
            newIcon = Icon.createWithResource(applicationContext, android.R.drawable.ic_menu_search)
            newState = Tile.STATE_ACTIVE

        } else {
            newLabel = String.format(Locale.US, "%s %s", getString(R.string.app_name), getString(R.string.service_inactive))
            newIcon = Icon.createWithResource(applicationContext, android.R.drawable.ic_menu_close_clear_cancel)
            newState = Tile.STATE_INACTIVE
        }

        // Change the UI of the tile.
        tile.label = newLabel
        tile.icon = newIcon
        tile.state = newState

        // Need to call updateTile for the tile to pick up changes.
        tile.updateTile()

    }

    private fun toggleServiceStatus(): Boolean {

        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        var isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, false)
        isActive = !isActive
        if (!isActive) {
            Bus.send(InfoPanelDisabledEvent())
        }
        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, isActive).apply()

        return isActive
    }
}