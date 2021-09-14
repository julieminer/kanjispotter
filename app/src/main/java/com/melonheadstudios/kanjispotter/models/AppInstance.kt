package com.melonheadstudios.kanjispotter.models

import com.atilika.kuromoji.ipadic.Token
import kotlinx.coroutines.Deferred
import java.util.*

data class KanjiInstance(val token: Token, val dateSearched: Date, var englishReading: Deferred<String?>)