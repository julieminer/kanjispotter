package com.melonheadstudios.kanjispotter.repos

import android.content.Context
import android.content.Intent
import com.atilika.kuromoji.ipadic.Token
import com.atilika.kuromoji.ipadic.Tokenizer
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.models.KanjiInstance
import com.melonheadstudios.kanjispotter.services.AccessibilityEventHolder
import com.melonheadstudios.kanjispotter.services.HoverPanelService
import com.melonheadstudios.kanjispotter.utils.Constants
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap

/**
 * kanjispotter
 * Created by jake on 2018-05-28, 7:32 PM
 */
@Singleton
class KanjiRepo(private val applicationContext: Context) {
    @Inject
    lateinit var tokenizer: Tokenizer

    @Inject
    lateinit var moshi: Moshi

    init {
        MainApplication.graph.inject(this)
    }

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

    fun add(kanji: Token, forApp: String) = async(UI) {
        if (kanjiAppDictionary[forApp] == null) {
            kanjiAppDictionary[forApp] = mutableListOf()
        }
        val kanjiInstance = KanjiInstance(kanji, Date())
        kanjiAppDictionary[forApp]?.add(kanjiInstance)
    }

    fun clear(fromApp: String) {
        kanjiAppDictionary[fromApp]?.clear()
    }

    fun clearAll() {
        kanjiAppDictionary.clear()
    }

    fun parse(event: AccessibilityEventHolder) = async(UI) {
        val app = event.packageName
        val text = event.text
        val tokens = tokenizer.tokenize(text) ?: return@async
        val knownTokens = tokens.filter { it.isKnown }

        if (knownTokens.isEmpty()) {
            return@async
        }

        if (!applicationContext.isServiceRunning(HoverPanelService::class.java)) {
            val startHoverIntent = Intent(applicationContext, HoverPanelService::class.java)
            applicationContext.startService(startHoverIntent)
        }

        knownTokens.forEach {
            if (!has(it.baseForm)) {
                add(it, app)
            }
        }

        Answers.getInstance().logCustom(CustomEvent(Constants.EVENT_API))
    }
}
