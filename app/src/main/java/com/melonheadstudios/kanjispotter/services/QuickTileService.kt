package com.melonheadstudios.kanjispotter.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.squareup.otto.Subscribe
import java.util.*
import javax.inject.Inject

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 12:22 AM
 */
@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
class QuickTileService: TileService() {
    override fun onCreate() {
        super.onCreate()
        MainApplication.instance.bus.register(this)
    }

    override fun onDestroy() {
        MainApplication.instance.bus.unregister(this)
        super.onDestroy()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        toggleServiceStatus()
    }

    private fun updateTile() {
        val tile = this.qsTile
        val isActive = MainApplication.instance.prefManager.serviceStatus()

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

    @SuppressLint("CommitPrefEdits")
    private fun toggleServiceStatus(): Boolean {
        var isActive = MainApplication.instance.prefManager.serviceStatus()
        isActive = !isActive
        MainApplication.instance.prefManager.serviceStatus(isActive)
        MainApplication.instance.bus.post(InfoPanelPreferenceChanged(isActive))
        return isActive
    }

    @Subscribe
    fun onPrefChangedEvent(it: InfoPanelPreferenceChanged) {
        updateTile()
    }
}