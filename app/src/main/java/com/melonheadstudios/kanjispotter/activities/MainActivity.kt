package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.jrejaud.onboarder.OnboardingActivity
import com.jrejaud.onboarder.OnboardingPage
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.models.IABUpdateUIEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.APP_BLACKLISTED
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.BLACKLIST_SELECTION_STATUS_FLAG
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.BLACKLIST_STATUS_FLAG
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.PREFERENCES_KEY
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.SERVICE_STATUS_FLAG
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var iabManager: IABManager

    private val fastAdapter = FastAdapter<BlacklistSelectionModel>()
    private val itemAdapter = ItemAdapter<BlacklistSelectionModel>()
    private var items = ArrayList<BlacklistSelectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        if (shouldLaunchOnboarding()) {
            launchOnboarding()
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
        if (isPremium != null) {
            remove_ads_button.visibility = if (isPremium) GONE else VISIBLE
        }
        val overlayEnabled = isOverlayEnabled()
        spotter_overlay_switch.isChecked = overlayEnabled
        blacklist_check_container.visibility = if (overlayEnabled) VISIBLE else GONE

        val blacklistEnabled = overlayEnabled && isBlacklistEnabled()
        blacklist_switch.isChecked = blacklistEnabled
        blacklist_list.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_container.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_check.isChecked = allBlacklistChecked()
        if (blacklistEnabled) {
            populateBlacklist(forceRepopulate = forceRepopulate)
        }
    }

    private fun populateBlacklist(forceRepopulate: Boolean = false) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)

        if (!forceRepopulate && items.isNotEmpty()) return

        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        items.clear()
        for (app in pkgAppsList) {
            val appLabel = app.loadLabel(packageManager).toString()
            val packageName = app.activityInfo.taskAffinity ?: continue
            val packageIcon = app.loadIcon(packageManager)
            items.add(BlacklistSelectionModel(sharedPreferences = prefs, appName = appLabel, packageName = packageName, icon = packageIcon))
        }
        items.sortBy { it.appName }
        itemAdapter.set(items)
    }

    @SuppressLint("CommitPrefEdits")
    private fun setOverlayEnabled(enabled: Boolean) {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, enabled).commit()
        Bus.send(InfoPanelPreferenceChanged(enabled = enabled))
    }

    private fun isOverlayEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SERVICE_STATUS_FLAG, true)
    }

    private fun isBlacklistEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(BLACKLIST_STATUS_FLAG, false)
    }

    private fun setBlacklistEnabled(enabled: Boolean) {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(BLACKLIST_STATUS_FLAG, enabled).apply()
        if (enabled) {
            populateBlacklist()
        }
    }

    private fun allBlacklistChecked(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(BLACKLIST_SELECTION_STATUS_FLAG, false)
    }

    @SuppressLint("CommitPrefEdits")
    private fun selectAllBlacklist(selectedAll: Boolean) {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putBoolean(BLACKLIST_SELECTION_STATUS_FLAG, selectedAll)
        for (item in items) {
            edit.putBoolean(APP_BLACKLISTED + item.packageName, selectedAll)
        }
        edit.commit()

        itemAdapter.set(items)
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
