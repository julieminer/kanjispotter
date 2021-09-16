package com.melonheadstudios.kanjispotter.repos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.atilika.kuromoji.ipadic.Token
import com.atilika.kuromoji.ipadic.Tokenizer
import com.google.firebase.analytics.FirebaseAnalytics
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.models.KanjiInstance
import com.melonheadstudios.kanjispotter.models.englishDefinition
import com.melonheadstudios.kanjispotter.services.AccessibilityEventHolder
import com.melonheadstudios.kanjispotter.services.HoverPanelService
import com.melonheadstudios.kanjispotter.services.JishoService
import com.melonheadstudios.kanjispotter.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * kanjispotter
 * Created by jake on 2018-05-28, 7:32 PM
 */
class KanjiRepo(private val appContext: Context, private val tokenizer: Tokenizer, private val appScope: CoroutineScope, private val jishoService: JishoService) {
    private var kanjiAppDictionary = HashMap<String, MutableList<KanjiInstance>>()
    private val mutableFilteredKanji = MutableStateFlow<Set<KanjiInstance>>(setOf())
    private val mutableParsedKanji = MutableStateFlow<Set<KanjiInstance>>(setOf())

    val parsedKanji = mutableParsedKanji.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)
    val filteredKanji = mutableFilteredKanji.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)

    private fun has(kanji: String): Boolean {
        return allKanji().map { it.baseForm }.contains(kanji)
    }

    private fun allKanji(): Set<KanjiInstance> {
        return kanjiAppDictionary.values.flatMap { it }.sortedByDescending { it.dateSearched.time }.toSet()
    }

    private suspend fun add(kanji: Token, forApp: String) {
        if (kanjiAppDictionary[forApp] == null) {
            kanjiAppDictionary[forApp] = mutableListOf()
        }
        val kanjiInstance = KanjiInstance(kanji.baseForm.trim(), kanji.reading.trim(), Date(), appScope.async { jishoService.get(kanji.baseForm)?.englishDefinition() } )
        if (kanjiInstance.baseForm.isBlank()) {
            return
        }
        kanjiAppDictionary[forApp]?.add(kanjiInstance)
    }

    fun clearAll() = appScope.launch  {
        kanjiAppDictionary.clear()
        mutableParsedKanji.emit(allKanji())
    }

    fun toggleFilter(kanjiInstance: KanjiInstance) = appScope.launch {
        var filteredKanji = mutableFilteredKanji.value
        filteredKanji = if (filteredKanji.contains(kanjiInstance)) {
            filteredKanji.filter { it != kanjiInstance }.toSet()
        } else {
            filteredKanji + kanjiInstance
        }
        mutableFilteredKanji.emit(filteredKanji)
    }

    fun toggleAllClicked() = appScope.launch {
        val filteredKanji = mutableFilteredKanji.value
        val allKanji = allKanji()
        when (filteredKanji.count()) {
            0 -> mutableFilteredKanji.emit(allKanji)
            else -> mutableFilteredKanji.emit(setOf())
        }
    }

    fun parse(event: AccessibilityEventHolder) = appScope.launch {
        val app = event.packageName
        val text = event.text
        val tokens = tokenizer.tokenize(text) ?: return@launch
        val knownTokens = tokens.filter { it.isKnown }

        if (knownTokens.isEmpty()) {
            return@launch
        }

        if (!appContext.isServiceRunning(HoverPanelService::class.java)) {
            val startHoverIntent = Intent(appContext, HoverPanelService::class.java)
            appContext.startService(startHoverIntent)
        }

        knownTokens.forEach {
            if (!has(it.baseForm)) {
                add(it, app)
            }
        }
        mutableParsedKanji.emit(allKanji())
        mutableFilteredKanji.emit(setOf())

        FirebaseAnalytics.getInstance(appContext).logEvent(Constants.EVENT_API, Bundle())
    }
}
