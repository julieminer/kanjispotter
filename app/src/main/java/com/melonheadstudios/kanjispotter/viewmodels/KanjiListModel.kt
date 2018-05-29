package com.melonheadstudios.kanjispotter.viewmodels

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.extensions.saveToClipboard
import com.melonheadstudios.kanjispotter.models.JishoModel
import com.melonheadstudios.kanjispotter.models.englishDefinition
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.utils.ViewHolderFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import javax.inject.Inject
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * kanjispotter
 * Created by jake on 2017-04-15, 9:48 PM
 */
class KanjiListModel(private val kanjiText: String, private val readingText: String, val selectedWord: String): AbstractItem<KanjiListModel, KanjiListModel.ViewHolder>() {
    var englishReading: String? = null

    override fun getType(): Int {
        return R.id.KANJI_LIST_MODEL
    }

    override fun getLayoutRes(): Int {
        return R.layout.list_item
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>?) {
        super.bindView(holder, payloads)
        holder.kanjiText.text = kanjiText
        holder.furiganaText.text = readingText
        holder.kanjiText.saveToClipboard(text = holder.kanjiText.text as String)
        holder.furiganaContainer.saveToClipboard(text = holder.furiganaText.text as String)
        holder.englishReading = englishReading
        if (englishReading == null) {
            getDefinition(holder)
        }
    }

    override fun getFactory(): ViewHolderFactory<out ViewHolder> {
        return FACTORY
    }

    private class ItemFactory : ViewHolderFactory<ViewHolder> {
        override fun create(v: View): ViewHolder {
            val viewHolder = ViewHolder(v)
            MainApplication.graph.inject(viewHolder)
            return viewHolder
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        @Inject
        lateinit var moshi: Moshi

        var englishReading: String? = null
            set(value) {
                englishText.visibility = if (value.isNullOrEmpty()) GONE else VISIBLE
                englishText.text = value ?: ""
            }

        var kanjiText: TextView = view.findViewById(R.id.kanji_text)
        var furiganaText: TextView = view.findViewById(R.id.furigana_text)
        private var englishText: TextView = view.findViewById(R.id.english_text)
        var furiganaContainer: LinearLayout = view.findViewById(R.id.furigana_container)
    }

    companion object {
        private val FACTORY = ItemFactory()
    }

    override fun equals(other: Any?): Boolean {
        val rhs = other as? KanjiListModel ?: return false
        return rhs.kanjiText == kanjiText || rhs.readingText == readingText || rhs.selectedWord == selectedWord
    }

    override fun hashCode(): Int {
        return (kanjiText + readingText).hashCode()
    }

    private fun getDefinition(holder: ViewHolder) = async(UI) {
        val english = getJishoModel(kanjiText, holder)?.englishDefinition()
        englishReading = english
        holder.englishReading = englishReading
    }

    private suspend fun getJishoModel(forKanji: String, holder: ViewHolder): JishoModel? = suspendCoroutine { continuation ->
        val dir = "http://jisho.org/api/v1/search/words?keyword="
        (dir + forKanji).httpGet().responseString { _, _, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    continuation.resume(null)
                }
                is Result.Success -> {
                    try {
                        val data = result.get()
                        val jsonAdapter = holder.moshi.adapter(JishoModel::class.java)
                        val response = jsonAdapter.fromJson(data)
                        continuation.resume(response)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Crashlytics.logException(e)
                        continuation.resume(null)
                    }
                }
            }
        }
    }
}
