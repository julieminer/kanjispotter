package com.melonheadstudios.kanjispotter.models

import kotlinx.coroutines.Deferred
import java.util.*

data class KanjiInstance(val baseForm: String,
                         val reading: String,
                         val dateSearched: Date,
                         var englishReading: Deferred<String?>)

