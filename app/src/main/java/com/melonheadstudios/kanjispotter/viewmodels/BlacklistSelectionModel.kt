package com.melonheadstudios.kanjispotter.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.services.QuickTileService
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.utils.ViewHolderFactory

/**
 * kanjispotter
 * Created by jake on 2017-04-17, 11:45 AM
 */

class BlacklistSelectionModel(val sharedPreferences: SharedPreferences, val appName: String, val packageName: String, val icon: Drawable): AbstractItem<BlacklistSelectionModel, BlacklistSelectionModel.ViewHolder>() {
    override fun getType(): Int {
        return R.id.BLACKLIST_SELECTION_MODEL
    }

    override fun getLayoutRes(): Int {
        return R.layout.blacklist_item
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>?) {
        super.bindView(holder, payloads)
        holder.appName.text = appName
        holder.checkBox.isChecked = isItemChecked()
        holder.appIcon.setImageDrawable(icon)
        holder.container.setOnClickListener { holder.checkBox.performClick() }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked -> setItemChecked(isChecked) }
    }

    @SuppressLint("CommitPrefEdits")
    private fun setItemChecked(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(QuickTileService.APP_BLACKLISTED + packageName, isChecked).commit()
    }

    private fun isItemChecked(): Boolean {
        return sharedPreferences.getBoolean(QuickTileService.APP_BLACKLISTED + packageName, false)
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
        var appIcon: ImageView = view.findViewById(R.id.app_icon) as ImageView
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
            }
        }
    }
}