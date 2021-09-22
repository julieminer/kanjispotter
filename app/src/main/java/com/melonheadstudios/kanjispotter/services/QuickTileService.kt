package com.melonheadstudios.kanjispotter.services

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.melonheadstudios.kanjispotter.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 12:22 AM
 */
class QuickTileService: TileService(), KoinComponent {
    private val appScope: CoroutineScope by inject()
    private val preferencesService: PreferencesService by inject()

    override fun onStartListening() {
        super.onStartListening()
        appScope.launch {
            preferencesService.overlayEnabled.collect {
                updateTile(isActive = it == true)
            }
        }
    }

    override fun onClick() {
        runBlocking {
            val isActive = preferencesService.overlayEnabled.firstOrNull() ?: false
            preferencesService.setOverlayEnabled(!isActive)
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