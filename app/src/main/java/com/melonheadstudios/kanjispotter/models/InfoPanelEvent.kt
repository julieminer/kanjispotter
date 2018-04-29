package com.melonheadstudios.kanjispotter.models

import com.atilika.kuromoji.ipadic.Token

class TokenizedEvent(val token: Token, val jishoModel: JishoModel?)
class InfoPanelSelectionsEvent(val selections: List<String>)
class InfoPanelSelectedWordEvent(val position: Int)
class InfoPanelPreferenceChanged(val enabled: Boolean)
class InfoPanelErrorEvent(val errorText: String, val showHeaders: Boolean = false)
