package com.melonheadstudios.kanjispotter.models

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 4:19 PM
 */

class InfoPanelEvent(val chosenWord: String, val json: String)
class InfoPanelClearEvent
class InfoPanelSelectionsEvent(val selections: List<String>)
