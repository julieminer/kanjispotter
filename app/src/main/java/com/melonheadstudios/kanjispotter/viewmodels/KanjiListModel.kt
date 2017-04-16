package com.melonheadstudios.kanjispotter.viewmodels

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.melonheadstudios.kanjispotter.R
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.utils.ViewHolderFactory

/**
 * kanjispotter
 * Created by jake on 2017-04-15, 9:48 PM
 */
class KanjiListModel(val kanjiText: String, private val readingText: String, val selectedWord: String): AbstractItem<KanjiListModel, KanjiListModel.ViewHolder>() {
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
    }

    override fun getFactory(): ViewHolderFactory<out ViewHolder> {
        return FACTORY
    }

    private class ItemFactory : ViewHolderFactory<ViewHolder> {
        override fun create(v: View): ViewHolder {
            return ViewHolder(v)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var kanjiText: TextView = view.findViewById(R.id.kanji_text) as TextView
        var furiganaText: TextView = view.findViewById(R.id.furigana_text) as TextView
    }

    companion object {
        private val FACTORY = ItemFactory()
    }

    override fun equals(other: Any?): Boolean {
        val rhs = other as? KanjiListModel ?: return false
        return rhs.kanjiText == kanjiText && rhs.readingText == readingText
    }

    override fun hashCode(): Int {
        return kanjiText.hashCode() + readingText.hashCode()
    }
}
