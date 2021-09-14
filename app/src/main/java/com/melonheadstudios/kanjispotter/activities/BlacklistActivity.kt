package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.services.DataStore
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.actvity_blacklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.*

/**
 * kanjispotter
 * Created by jake on 2017-04-22, 12:21 AM
 */
class BlacklistActivity: AppCompatActivity() {
//    private val dataStore: DataStore by inject()
//
//    private lateinit var fastAdapter: FastAdapter<BlacklistSelectionModel>
//    private lateinit var itemAdapter: ItemAdapter<BlacklistSelectionModel>
//    private lateinit var items: ArrayList<BlacklistSelectionModel>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        title = "Blacklist Settings"
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        val darkThemeEnabled = runBlocking { dataStore.darkThemeEnabled.firstOrNull() == true }
//        val blackListEnabled = runBlocking { dataStore.blackListEnabled.firstOrNull() == true }
//        val blackListAllEnabled = runBlocking { dataStore.blackListAllEnabled.firstOrNull() == true }
//        if (!darkThemeEnabled) {
//            setTheme(R.style.AppThemeLight)
//        }
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.actvity_blacklist)
//
//        items = ArrayList()
//        fastAdapter = FastAdapter()
//        itemAdapter = ItemAdapter()
//
//        blacklist_progress.visibility = VISIBLE
//
//        blacklist_switch.setOnCheckedChangeListener { _, isChecked ->
//            setBlacklistEnabled(isChecked)
//            updateUI()
//        }
//
//        blacklist_all_check.setOnCheckedChangeListener { _, isChecked ->
//            selectAllBlacklist(isChecked)
//            updateUI()
//        }
//
//        blacklist_switch.isChecked = blackListEnabled
//        blacklist_all_check.isChecked = blackListAllEnabled
//
//        blacklist_list.layoutManager =
//            LinearLayoutManager(this)
//        blacklist_list.itemAnimator =
//            DefaultItemAnimator()
//        blacklist_list.adapter = itemAdapter.wrap(fastAdapter)
//
//        fastAdapter.withItemEvent(BlacklistSelectionModel.CheckButtonClickEvent())
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateUI(forceRepopulate = true)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            onBackPressed()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun populateBlacklist(forceRepopulate: Boolean = false) = lifecycleScope.launch(Dispatchers.Main) {
//        blacklist_progress.visibility = VISIBLE
//        val mainIntent = Intent(Intent.ACTION_MAIN, null)
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
//        val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
//
//        if (!forceRepopulate && items.isNotEmpty()) {
//            blacklist_progress.visibility = GONE
//        } else {
//            items.clear()
//            for (app in pkgAppsList) {
//                val appLabel = app.loadLabel(packageManager).toString()
//                val packageName = app.activityInfo.taskAffinity ?: continue
//                val packageIcon = app.loadIcon(packageManager)
//                items.add(BlacklistSelectionModel(sharedPreferences = prefManager.prefs, appName = appLabel, packageName = packageName, icon = packageIcon))
//            }
//            items.sortBy { it.appName }
//            itemAdapter.set(items)
//            blacklist_progress.visibility = GONE
//        }
//
//    }
//
//    private fun updateUI(forceRepopulate: Boolean = false) = lifecycleScope.launch(Dispatchers.Main) {
//        val overlayEnabled = prefManager.overlayEnabled()
//        blacklist_check_container.visibility = if (overlayEnabled) VISIBLE else GONE
//
//        val blacklistEnabled = overlayEnabled && prefManager.blacklistEnabled()
//        blacklist_progress.visibility = if (!blacklistEnabled) GONE else blacklist_progress.visibility
//        blacklist_switch.isChecked = blacklistEnabled
//        blacklist_list.visibility = if (blacklistEnabled) VISIBLE else GONE
//        blacklist_all_container.visibility = if (blacklistEnabled) VISIBLE else GONE
//        blacklist_all_check.isChecked = prefManager.allBlackListChecked()
//        if (blacklistEnabled) {
//            populateBlacklist(forceRepopulate = forceRepopulate)
//        }
//    }
//
//    private fun setBlacklistEnabled(enabled: Boolean) = lifecycleScope.launch {
//        blacklist_progress.visibility = VISIBLE
//        dataStore.setBlackListEnabled(enabled)
//    }
//
//    @SuppressLint("CommitPrefEdits")
//    private fun selectAllBlacklist(selectedAll: Boolean) = lifecycleScope.launch(Dispatchers.Main) {
//        dataStore.setBlackListAllEnabled(selectedAll)
//        blacklist_progress.visibility = VISIBLE
//        prefManager.setAllAppsBlackilist(selectedAll, items)
//        itemAdapter.set(items)
//        blacklist_progress.visibility = GONE
//    }
}