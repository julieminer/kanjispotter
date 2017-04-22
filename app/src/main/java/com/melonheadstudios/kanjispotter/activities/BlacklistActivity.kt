package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.actvity_blacklist.*
import java.util.ArrayList
import javax.inject.Inject

/**
 * kanjispotter
 * Created by jake on 2017-04-22, 12:21 AM
 */
class BlacklistActivity: AppCompatActivity() {
    @Inject
    lateinit var prefManager: PrefManager

    private val fastAdapter = FastAdapter<BlacklistSelectionModel>()
    private val itemAdapter = ItemAdapter<BlacklistSelectionModel>()
    private var items = ArrayList<BlacklistSelectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        title = "Blacklist Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_blacklist)

        blacklist_switch.setOnClickListener {
            val isChecked = !prefManager.blacklistEnabled()
            setBlacklistEnabled(isChecked)
            updateUI()
        }

        blacklist_all_check.setOnCheckedChangeListener { _, isChecked ->
            selectAllBlacklist(isChecked)
            updateUI()
        }

        blacklist_list.layoutManager = LinearLayoutManager(this)
        blacklist_list.layoutManager.isAutoMeasureEnabled = true
        blacklist_list.itemAnimator = DefaultItemAnimator()
        blacklist_list.adapter = itemAdapter.wrap(fastAdapter)

        fastAdapter.withItemEvent(BlacklistSelectionModel.CheckButtonClickEvent())
    }

    override fun onResume() {
        super.onResume()
        updateUI(forceRepopulate = true)
    }

    // TODO move to async task
    private fun populateBlacklist(forceRepopulate: Boolean = false) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)

        if (!forceRepopulate && items.isNotEmpty()) return

        items.clear()
        for (app in pkgAppsList) {
            val appLabel = app.loadLabel(packageManager).toString()
            val packageName = app.activityInfo.taskAffinity ?: continue
            val packageIcon = app.loadIcon(packageManager)
            items.add(BlacklistSelectionModel(sharedPreferences = prefManager.prefs, appName = appLabel, packageName = packageName, icon = packageIcon))
        }
        items.sortBy { it.appName }
        itemAdapter.set(items)
    }

    private fun updateUI(forceRepopulate: Boolean = false) {
        val overlayEnabled = prefManager.overlayEnabled()
        blacklist_check_container.visibility = if (overlayEnabled) View.VISIBLE else View.GONE

        val blacklistEnabled = overlayEnabled && prefManager.blacklistEnabled()
        blacklist_switch.isChecked = blacklistEnabled
        blacklist_list.visibility = if (blacklistEnabled) View.VISIBLE else View.GONE
        blacklist_all_container.visibility = if (blacklistEnabled) View.VISIBLE else View.GONE
        blacklist_all_check.isChecked = prefManager.allBlackListChecked()
        if (blacklistEnabled) {
            populateBlacklist(forceRepopulate = forceRepopulate)
        }
    }

    private fun setBlacklistEnabled(enabled: Boolean) {
        prefManager.setBlacklist(enabled)
        if (enabled) {
            populateBlacklist()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun selectAllBlacklist(selectedAll: Boolean) {
        prefManager.setAllBlackListChecked(selectedAll, items)
        itemAdapter.set(items)
    }
}