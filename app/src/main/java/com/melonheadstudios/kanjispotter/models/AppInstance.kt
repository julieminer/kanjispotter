package com.melonheadstudios.kanjispotter.models

import android.arch.lifecycle.MutableLiveData
import com.atilika.kuromoji.ipadic.Token
import java.util.*

data class KanjiInstance(val token: Token, val dateSearched: Date, var reading: String? = null)