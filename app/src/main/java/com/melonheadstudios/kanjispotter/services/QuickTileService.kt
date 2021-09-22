package com.melonheadstudios.kanjispotter.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 12:22 AM
 */
@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
class QuickTileService: TileService() {
    private val app: MainApplication by lazy { MainApplication.instance }

    override fun onStartListening() {
        super.onStartListening()
        app.scope.launch {
            app.preferencesService.overlayEnabled.collect {
                updateTile(isActive = it == true)
            }
        }
    }

    override fun onClick() {
        runBlocking {
            val isActive = app.preferencesService.overlayEnabled.firstOrNull() ?: false
            app.preferencesService.setOverlayEnabled(!isActive)
        }
    }

    private fun updateTile(isActive: Boolean) {
        val tile = this.qsTile

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
}