package com.melonheadstudios.kanjispotter.repos

import android.content.Context
import android.os.Bundle
import com.atilika.kuromoji.ipadic.Tokenizer
import com.google.firebase.analytics.FirebaseAnalytics
import com.melonheadstudios.kanjispotter.extensions.toKanji
import com.melonheadstudios.kanjispotter.models.Kanji
import com.melonheadstudios.kanjispotter.services.AccessibilityEventHolder
import com.melonheadstudios.kanjispotter.services.JishoService
import com.melonheadstudios.kanjispotter.utils.Constants
import com.melonheadstudios.kanjispotter.utils.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * kanjispotter
 * Created by jake on 2018-05-28, 7:32 PM
 */
class KanjiRepo(private val appContext: Context,
                private val tokenizer: Tokenizer,
                private val appScope: CoroutineScope,
                private val jishoService: JishoService,
                private val notificationManager: NotificationManager) {
    private var kanjiAppDictionary = HashMap<String, MutableList<Kanji>>()
    private val mutableFilteredKanji = MutableStateFlow<Set<Kanji>>(setOf())
    private val mutableParsedKanji = MutableStateFlow<Set<Kanji>>(setOf())

    val parsedKanji = mutableParsedKanji.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)
    val filteredKanji = mutableFilteredKanji.shareIn(appScope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 10000), replay = 1)

    private fun has(kanji: String): Boolean {
        return allKanji().map { it.baseForm }.contains(kanji)
    }

    private fun allKanji(): Set<Kanji> {
        return kanjiAppDictionary.values.flatMap { it }.sortedByDescending { it.dateSearched.time }.toSet()
    }

    private fun add(kanji: Kanji, forApp: String) {
        if (kanjiAppDictionary[forApp] == null) {
            kanjiAppDictionary[forApp] = mutableListOf()
        }
        if (kanji.baseForm.isBlank()) {
            return
        }
        kanjiAppDictionary[forApp]?.add(kanji)
    }

    fun toggleFilter(kanji: Kanji) = appScope.launch {
        var filteredKanji = mutableFilteredKanji.value
        filteredKanji = if (filteredKanji.contains(kanji)) {
            filteredKanji.filter { it != kanji }.toSet()
        } else {
            filteredKanji + kanji
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

    fun kanjiForText(text: String): Set<Kanji> {
        val tokens = tokenizer.tokenize(text) ?: return setOf()
        return tokens.filter { it.isKnown }.map { it.toKanji(appScope, jishoService) }.toSet()
    }

    fun parse(event: AccessibilityEventHolder) = appScope.launch {
        val app = event.packageName
        val text = event.text
        val knownTokens = kanjiForText(text)

        if (knownTokens.isEmpty()) {
            return@launch
        }

        knownTokens.forEach {
            if (!has(it.baseForm)) {
                add(it, app)
            }
        }
        mutableParsedKanji.emit(allKanji())
        mutableFilteredKanji.emit(setOf())
        notificationManager.showNotification()
        FirebaseAnalytics.getInstance(appContext).logEvent(Constants.EVENT_API, Bundle())
    }
}
