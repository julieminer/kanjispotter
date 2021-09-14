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
    private val mutableSelectedKanjiPosition = MutableStateFlow(0)
    private val mutableParsedKanji = MutableStateFlow<List<KanjiInstance>>(listOf())

    val parsedKanji = mutableParsedKanji.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)
    val selectedKanjiPosition = mutableSelectedKanjiPosition.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)

    private fun has(kanji: String): Boolean {
        return allKanji().map { it.token.baseForm }.contains(kanji)
    }

    fun allKanji(): List<KanjiInstance> {
        return kanjiAppDictionary.values.flatMap { it }.sortedByDescending { it.dateSearched.time }
    }

    private suspend fun add(kanji: Token, forApp: String) {
        if (kanjiAppDictionary[forApp] == null) {
            kanjiAppDictionary[forApp] = mutableListOf()
        }
        val kanjiInstance = KanjiInstance(kanji, Date(), appScope.async { jishoService.get(kanji.baseForm)?.englishDefinition() } )
        kanjiAppDictionary[forApp]?.add(kanjiInstance)
    }

    fun clearAll() = appScope.launch  {
        kanjiAppDictionary.clear()
        mutableParsedKanji.emit(allKanji())
    }

    fun select(kanjiPosition: Int) = appScope.launch {
        mutableSelectedKanjiPosition.emit(kanjiPosition)
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

        FirebaseAnalytics.getInstance(appContext).logEvent(Constants.EVENT_API, Bundle())
    }
}
