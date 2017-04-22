package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.IntentCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.IABUpdateUIEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.services.InfoPanelDisplayService
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var iabManager: IABManager

    @Inject
    lateinit var prefManager: PrefManager

    private val fastAdapter = FastAdapter<BlacklistSelectionModel>()
    private val itemAdapter = ItemAdapter<BlacklistSelectionModel>()
    private var items = ArrayList<BlacklistSelectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        if (shouldLaunchOnboarding()) {
            startActivity(Intent(this, KanjiOnboardingActivity::class.java))
        }

        Bus.observe<InfoPanelPreferenceChanged>()
                .subscribe { updateUI() }
                .registerInBus(this)

        Bus.observe<IABUpdateUIEvent>()
                .subscribe {
                    Log.d("IABManager", "premium status update ${it.isPremium}")
                    updateUI(forceRepopulate = false, isPremium = it.isPremium)
                }
                .registerInBus(this)

        report_issue_button.setOnClickListener {
            reportIssue()
        }

        remove_ads_button.setOnClickListener {
            iabManager.onUpgradeAppButtonClicked(this)
        }

        blacklist_switch.setOnClickListener {
            val isChecked = !prefManager.blacklistEnabled()
            setBlacklistEnabled(isChecked)
            updateUI()
        }

        blacklist_all_check.setOnCheckedChangeListener { _, isChecked ->
            selectAllBlacklist(isChecked)
            updateUI()
        }

        spotter_overlay_switch.setOnClickListener {
            val isChecked = !prefManager.overlayEnabled()
            setOverlayEnabled(isChecked)
            updateUI()
        }

        theme_dark_switch.setOnClickListener {
            val isChecked = !prefManager.darkThemeEnabled()
            setDarkThemeEnabled(isChecked)
        }

        blacklist_list.layoutManager = LinearLayoutManager(this)
        blacklist_list.layoutManager.isAutoMeasureEnabled = true
        blacklist_list.itemAnimator = DefaultItemAnimator()
        blacklist_list.adapter = itemAdapter.wrap(fastAdapter)

        fastAdapter.withItemEvent(BlacklistSelectionModel.CheckButtonClickEvent())

        iabManager.setupIAB(context = this)
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI(forceRepopulate = true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        iabManager.handleResult(requestCode, resultCode, data) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        iabManager.unregister(this)
        super.onDestroy()
    }

    private fun updateUI(forceRepopulate: Boolean = false, isPremium: Boolean? = null) {
        if (BuildConfig.DEBUG) {
            remove_ads_button.visibility = GONE 
        }
        if (isPremium != null) {
            remove_ads_button.visibility = if (isPremium) GONE else VISIBLE
        }
        val overlayEnabled = prefManager.overlayEnabled()
        spotter_overlay_switch.isChecked = overlayEnabled
        blacklist_check_container.visibility = if (overlayEnabled) VISIBLE else GONE

        val blacklistEnabled = overlayEnabled && prefManager.blacklistEnabled()
        blacklist_switch.isChecked = blacklistEnabled
        blacklist_list.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_container.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_check.isChecked = prefManager.allBlackListChecked()
        theme_dark_switch.isChecked = prefManager.darkThemeEnabled()
        if (blacklistEnabled) {
            populateBlacklist(forceRepopulate = forceRepopulate)
        }
    }

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

    @SuppressLint("CommitPrefEdits")
    private fun setOverlayEnabled(enabled: Boolean) {
        prefManager.setOverlay(enabled)
        Bus.send(InfoPanelPreferenceChanged(enabled = enabled))
    }

    private fun setDarkThemeEnabled(enabled: Boolean) {
        prefManager.setDarkTheme(enabled)

        if (isMyServiceRunning(InfoPanelDisplayService::class.java)) {
            val service = Intent(applicationContext, InfoPanelDisplayService::class.java)
            stopService(service)
        }
        this.finish()
        val intent = this.intent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
        this.startActivity(intent)
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

    @SuppressLint("NewApi")
    private fun shouldLaunchOnboarding(): Boolean {
        val needsPermission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        val hasPermission = Settings.canDrawOverlays(this)
        val canDrawOverApps = !needsPermission || (needsPermission && hasPermission)
        val serviceIsRunning = isMyServiceRunning(JapaneseTextGrabberService::class.java)
        return !(canDrawOverApps && serviceIsRunning)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }

    private fun reportIssue() {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "melonheadstudiosapps@gmail.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Issue report")
        val userInfo = "Build Number: ${BuildConfig.VERSION_CODE}\n Version: ${BuildConfig.VERSION_NAME}\n " +
                "Model: ${Build.MODEL}\n Manufacturer: ${Build.MANUFACTURER}\n" +
                "API Version: ${Build.VERSION.SDK_INT}\n Android Version ${Build.VERSION.RELEASE}\n "
        emailIntent.putExtra(Intent.EXTRA_TEXT, userInfo)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}
