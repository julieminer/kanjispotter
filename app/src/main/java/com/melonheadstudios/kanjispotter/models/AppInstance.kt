package com.melonheadstudios.kanjispotter.models

import com.atilika.kuromoji.ipadic.Token
import java.util.*

data class KanjiInstance(val token: Token, val dateSearched: Date, var englishReading: String? = null)