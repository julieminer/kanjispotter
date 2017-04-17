package com.melonheadstudios.kanjispotter.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import java.util.*

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 12:22 AM
 */
@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
class QuickTileService: TileService() {

    override fun onCreate() {
        super.onCreate()

        Bus.observe<InfoPanelPreferenceChanged>()
                .subscribe { updateTile() }
                .registerInBus(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val SERVICE_STATUS_FLAG = "serviceStatus"
        val BLACKLIST_STATUS_FLAG = "blacklistEnabled"
        val BLACKLIST_SELECTION_STATUS_FLAG = "blacklistAllChecked"
        val PREFERENCES_KEY = "com.melonhead.android_quick_settings"
    }

    override fun onClick() {
        toggleServiceStatus()
    }

    private fun updateTile() {
        val tile = this.qsTile
        val isActive = getServiceStatus()

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

    private fun getServiceStatus(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SERVICE_STATUS_FLAG, true)
    }

    @SuppressLint("CommitPrefEdits")
    private fun toggleServiceStatus(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        var isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, true)
        isActive = !isActive
        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, isActive).commit()
        Bus.send(InfoPanelPreferenceChanged(isActive))

        return isActive
    }
}