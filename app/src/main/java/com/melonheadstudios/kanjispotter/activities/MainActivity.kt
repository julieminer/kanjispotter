package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.melonheadstudios.kanjispotter.R
import com.jrejaud.onboarder.OnboardingPage
import java.util.*
import com.jrejaud.onboarder.OnboardingActivity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import android.view.View.GONE
import android.view.View.VISIBLE
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.services.QuickTileService
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val fastAdapter = FastAdapter<BlacklistSelectionModel>()
    val itemAdapter = ItemAdapter<BlacklistSelectionModel>()
    var items = ArrayList<BlacklistSelectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (shouldLaunchOnboarding()) {
            launchOnboarding()
        }

        Bus.observe<InfoPanelPreferenceChanged>()
                .subscribe { updateUI() }
                .registerInBus(this)

        report_issue_button.setOnClickListener {
            // TODO open email to github or something
        }

        remove_ads_button.setOnClickListener {
            // TODO open IAP
        }

        blacklist_switch.setOnClickListener {
            val isChecked = !isBlacklistEnabled()
            setBlacklistEnabled(isChecked)
            updateUI()
        }

        blacklist_all_check.setOnCheckedChangeListener { _, isChecked ->
            selectAllBlacklist(isChecked)
            updateUI()
        }

        spotter_overlay_switch.setOnClickListener {
            val isChecked = !isOverlayEnabled()
            setOverlayEnabled(isChecked)
            updateUI()
        }

        blacklist_list.layoutManager = LinearLayoutManager(this)
        blacklist_list.layoutManager.isAutoMeasureEnabled = true
        blacklist_list.itemAnimator = DefaultItemAnimator()
        blacklist_list.adapter = itemAdapter.wrap(fastAdapter)

        fastAdapter.withItemEvent(BlacklistSelectionModel.CheckButtonClickEvent())

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        remove_ads_button.visibility = if (hasPurchasedPro()) GONE else VISIBLE
        val overlayEnabled = isOverlayEnabled()
        spotter_overlay_switch.isChecked = overlayEnabled
        blacklist_check_container.visibility = if (overlayEnabled) VISIBLE else GONE

        val blacklistEnabled = overlayEnabled && isBlacklistEnabled()
        blacklist_switch.isChecked = blacklistEnabled
        blacklist_list.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_container.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_check.isChecked = allBlacklistChecked()
        if (blacklistEnabled) {
            populateBlacklist()
        }
    }

    private fun populateBlacklist() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)

        items.clear()
        for (app in pkgAppsList) {
            val appLabel = app.loadLabel(packageManager).toString()
            val packageName = app.activityInfo.taskAffinity ?: continue
            items.add(BlacklistSelectionModel(appName = appLabel, packageName = packageName))
        }
        itemAdapter.set(items)
    }

    private fun hasPurchasedPro(): Boolean {
        // TODO check if purchased
        return false
    }

    @SuppressLint("CommitPrefEdits")
    private fun setOverlayEnabled(enabled: Boolean) {
        val prefs = applicationContext.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(QuickTileService.SERVICE_STATUS_FLAG, enabled).commit()
        Bus.send(InfoPanelPreferenceChanged(enabled = enabled))
    }

    private fun isOverlayEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(QuickTileService.SERVICE_STATUS_FLAG, true)
    }

    private fun isBlacklistEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(QuickTileService.BLACKLIST_STATUS_FLAG, false)
    }

    private fun setBlacklistEnabled(enabled: Boolean) {
        val prefs = applicationContext.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(QuickTileService.BLACKLIST_STATUS_FLAG, enabled).apply()
        if (enabled) {
            populateBlacklist()
        }
    }

    private fun allBlacklistChecked(): Boolean {
        val prefs = applicationContext.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(QuickTileService.BLACKLIST_SELECTION_STATUS_FLAG, false)
    }

    private fun selectAllBlacklist(selectedAll: Boolean) {
        val prefs = applicationContext.getSharedPreferences(QuickTileService.PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(QuickTileService.BLACKLIST_SELECTION_STATUS_FLAG, selectedAll).apply()
    }

    private fun launchOnboarding() {
        // TODO replace with real assets
        val page1 = OnboardingPage("Welcome to Kanji Spotter!", "You'll need to enable the app to draw over other apps on screen before we continue, though.", R.drawable.abc_ab_share_pack_mtrl_alpha, "Enable permission")
        val page2 = OnboardingPage(null, "Thanks! Now we'll just need to enable the accessibility service that powers the app!", R.drawable.abc_action_bar_item_background_material, "Enable Service")
        val page3 = OnboardingPage("Perfect!", "Test out the app by clicking the kanji below!\n 食べる、男の人、ご主人", R.drawable.abc_scrubber_control_to_pressed_mtrl_000, "Looking good!")

        val onboardingPages = LinkedList<OnboardingPage>()
        onboardingPages.add(page1)
        onboardingPages.add(page2)
        onboardingPages.add(page3)
        val onboardingActivityBundle = OnboardingActivity.newBundleColorBackground(android.R.color.white, onboardingPages)
        val intent = Intent(this, KanjiOnboardingActivity::class.java)
        intent.putExtras(onboardingActivityBundle)
        startActivity(intent)
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
}
