package com.melonheadstudios.kanjispotter.viewmodels

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.eightbitlab.rxbus.Bus
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.InfoPanelSelectedWordEvent
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.utils.ViewHolderFactory

/**
 * kanjispotter
 * Created by jake on 2017-04-17, 11:45 AM
 */

class BlacklistSelectionModel(val appName: String, val packageName: String): AbstractItem<BlacklistSelectionModel, BlacklistSelectionModel.ViewHolder>() {
    override fun getType(): Int {
        return R.id.BLACKLIST_SELECTION_MODEL
    }

    override fun getLayoutRes(): Int {
        return R.layout.blacklist_item
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>?) {
        super.bindView(holder, payloads)
        holder.appName.text = appName
        holder.checkBox.isChecked = isSelected
        holder.container.setOnClickListener { holder.checkBox.performClick() }
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

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var appName: TextView = view.findViewById(R.id.app_name) as TextView
        var checkBox: CheckBox = view.findViewById(R.id.app_blacklisted) as CheckBox
        var container: LinearLayout = view.findViewById(R.id.app_container) as LinearLayout
    }

    class CheckButtonClickEvent: ClickEventHook<BlacklistSelectionModel>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            val v = viewHolder as? BlacklistSelectionModel.ViewHolder ?: return null
            return v.checkBox
        }

        override fun onClick(v: View?, position: Int, fastAdapter: FastAdapter<BlacklistSelectionModel>?, item: BlacklistSelectionModel?) {
            item ?: return
            if (!item.isSelected) {
                fastAdapter?.select(position)
                Bus.send(InfoPanelSelectedWordEvent(position))
            }
        }
    }
}