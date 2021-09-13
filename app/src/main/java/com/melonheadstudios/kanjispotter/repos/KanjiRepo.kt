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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * kanjispotter
 * Created by jake on 2018-05-28, 7:32 PM
 */
class KanjiRepo(private val appContext: Context, private val tokenizer: Tokenizer, private val appScope: CoroutineScope, private val jishoService: JishoService) {
    private var kanjiAppDictionary = HashMap<String, MutableList<KanjiInstance>>()

    private fun has(kanji: String): Boolean {
        return allKanji().map { it.token.baseForm }.contains(kanji)
    }

    fun allKanji(): List<KanjiInstance> {
        return kanjiAppDictionary.values.flatMap { it }.sortedByDescending { it.dateSearched.time }
    }

    fun kanji(forApp: String): List<KanjiInstance> {
        return kanjiAppDictionary[forApp]?.sortedByDescending { it.dateSearched.time } ?: listOf()
    }

    fun add(kanji: Token, forApp: String) = appScope.launch(Dispatchers.Main) {
        if (kanjiAppDictionary[forApp] == null) {
            kanjiAppDictionary[forApp] = mutableListOf()
        }
        val kanjiInstance = KanjiInstance(kanji, Date(), jishoService.get(kanji.baseForm)?.englishDefinition())
        kanjiAppDictionary[forApp]?.add(kanjiInstance)
    }

    fun clear(fromApp: String) {
        kanjiAppDictionary[fromApp]?.clear()
    }

    fun clearAll() {
        kanjiAppDictionary.clear()
    }

    fun parse(event: AccessibilityEventHolder) = appScope.launch(Dispatchers.Main) {
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

        FirebaseAnalytics.getInstance(appContext).logEvent(Constants.EVENT_API, Bundle())
    }
}
