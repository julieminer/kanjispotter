package com.melonheadstudios.kanjispotter.models

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 4:19 PM
 */

class InfoPanelEvent(val chosenWord: String, val json: String)
class InfoPanelClearEvent
class InfoPanelSelectionsEvent(val selections: List<String>)
class InfoPanelSelectedWordEvent(val position: Int)
class InfoPanelPreferenceChanged(val enabled: Boolean)
class InfoPanelErrorEvent(val errorText: String, val showHeaders: Boolean = false)
