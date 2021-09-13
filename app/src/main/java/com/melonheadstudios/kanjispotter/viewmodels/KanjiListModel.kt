package com.melonheadstudios.kanjispotter.viewmodels

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.extensions.saveToClipboard
import com.melonheadstudios.kanjispotter.models.KanjiInstance
import com.melonheadstudios.kanjispotter.models.englishDefinition
import com.melonheadstudios.kanjispotter.services.JishoService
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.utils.ViewHolderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * kanjispotter
 * Created by jake on 2017-04-15, 9:48 PM
 */
class KanjiListModel(private val kanjiInstance: KanjiInstance, private val jishoService: JishoService): AbstractItem<KanjiListModel, KanjiListModel.ViewHolder>() {
    private val factory = ItemFactory()

//    private val kanjiText: String, private val readingText: String, val selectedWord: String
//    it.token.baseForm, it.token.reading, it.token.baseForm

    val kanjiText: String
        get() = kanjiInstance.token.baseForm

    val readingText: String
        get() = kanjiInstance.token.reading

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
        holder.englishReading = kanjiInstance.englishReading
        if (kanjiInstance.englishReading == null) {
            getDefinition(holder)
        }
    }

    override fun getFactory(): ViewHolderFactory<out ViewHolder> {
        return factory
    }

    private class ItemFactory() : ViewHolderFactory<ViewHolder> {
        override fun create(v: View): ViewHolder {
            val viewHolder = ViewHolder(v)
            return viewHolder
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

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

    override fun equals(other: Any?): Boolean {
        val rhs = other as? KanjiListModel ?: return false
        return rhs.kanjiText == kanjiText || rhs.readingText == readingText
    }

    override fun hashCode(): Int {
        return (kanjiText + readingText).hashCode()
    }

    private fun getDefinition(holder: ViewHolder) = GlobalScope.launch(Dispatchers.Main) {
        val english = jishoService.get(kanjiText)?.englishDefinition() ?: return@launch
        kanjiInstance.englishReading = english
        holder.englishReading = english
    }
}
