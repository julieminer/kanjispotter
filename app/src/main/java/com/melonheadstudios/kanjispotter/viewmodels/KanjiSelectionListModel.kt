package com.melonheadstudios.kanjispotter.viewmodels

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.extensions.saveToClipboard
import com.melonheadstudios.kanjispotter.models.InfoPanelSelectedWordEvent
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.EVENT_SWITCHED_WORDS
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.utils.ViewHolderFactory
import com.squareup.otto.Bus

/**
 * kanjispotter
 * Created by jake on 2017-04-15, 9:48 PM
 */
class KanjiSelectionListModel(val selectedWord: String, val bus: Bus): AbstractItem<KanjiSelectionListModel, KanjiSelectionListModel.ViewHolder>() {
    override fun getType(): Int {
        return R.id.KANJI_LIST_SELECTION_MODEL
    }

    override fun getLayoutRes(): Int {
        return R.layout.list_selection_item
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>?) {
        super.bindView(holder, payloads)
        holder.underline.visibility = if (isSelected) VISIBLE else GONE
        holder.selectedText.text = selectedWord
        holder.radiobutton.isChecked = isSelected
        holder.selectionBackground.setOnClickListener { holder.radiobutton.performClick() }
        holder.selectionBackground.saveToClipboard(text = holder.selectedText.text as String)
    }

    override fun getFactory(): ViewHolderFactory<out ViewHolder> {
        return FACTORY
    }

    private class ItemFactory : ViewHolderFactory<ViewHolder> {
        override fun create(v: View): ViewHolder {
            return ViewHolder(v)
        }
    }

    companion object {
        private val FACTORY = ItemFactory()
    }

    override fun equals(other: Any?): Boolean {
        val rhs = other as? KanjiSelectionListModel ?: return false
        return rhs.selectedWord == selectedWord
    }

    override fun hashCode(): Int {
        return selectedWord.hashCode()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var selectedText: TextView = view.findViewById(R.id.selected_word)
        var radiobutton: RadioButton = view.findViewById(R.id.radiobutton)
        var selectionBackground: LinearLayout = view.findViewById(R.id.selection_background)
        var underline: LinearLayout = view.findViewById(R.id.selected_underline)
    }

    class RadioButtonClickEvent: ClickEventHook<KanjiSelectionListModel>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            val v = viewHolder as? KanjiSelectionListModel.ViewHolder ?: return null
            return v.radiobutton
        }

        override fun onClick(v: View?, position: Int, fastAdapter: FastAdapter<KanjiSelectionListModel>?, item: KanjiSelectionListModel?) {
            item ?: return
            if (!item.isSelected) {
                val selections = fastAdapter?.selections ?: return
                if (!selections.isEmpty()) {
                    val selectedPosition = selections.iterator().next() ?: return
                    fastAdapter.deselect()
                    fastAdapter.notifyItemChanged(selectedPosition)
                }
                fastAdapter.select(position)
                item.bus.post(InfoPanelSelectedWordEvent(position))
                Answers.getInstance().logCustom(CustomEvent(EVENT_SWITCHED_WORDS))
            }
        }
    }
}
