package com.melonheadstudios.kanjispotter.models

import kotlinx.coroutines.Deferred
import java.util.*

data class Kanji(val baseForm: String,
                 val reading: String,
                 val dateSearched: Date,
                 var englishReading: Deferred<String?>)

