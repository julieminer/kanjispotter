package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.IntentCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.IABUpdateUIEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.services.InfoPanelDisplayService
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var iabManager: IABManager

    @Inject
    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        if (!prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (shouldLaunchOnboarding()) {
            startActivity(Intent(this, KanjiOnboardingActivity::class.java))
        }

        Bus.observe<InfoPanelPreferenceChanged>()
                .subscribe { updateUI() }
                .registerInBus(this)

        Bus.observe<IABUpdateUIEvent>()
                .subscribe {
                    Log.d("IABManager", "premium status update ${it.isPremium}")
                    updateUI(isPremium = it.isPremium || BuildConfig.DEBUG)
                }
                .registerInBus(this)

        report_issue_button.setOnClickListener {
            reportIssue()
        }

        remove_ads_button.setOnClickListener {
            iabManager.onUpgradeAppButtonClicked(this)
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

        blacklist_settings_button.setOnClickListener {
            val blacklistIntent = Intent(this, BlacklistActivity::class.java)
            startActivity(blacklistIntent)
        }

        iabManager.setupIAB(context = this)
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
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

    private fun updateUI(isPremium: Boolean? = null) {
        if (isPremium == null && BuildConfig.DEBUG) {
            remove_ads_button.visibility = GONE
        }
        if (isPremium != null) {
            remove_ads_button.visibility = if (isPremium) GONE else VISIBLE
        }
        val overlayEnabled = prefManager.overlayEnabled()
        spotter_overlay_switch.isChecked = overlayEnabled
        theme_dark_switch.isChecked = prefManager.darkThemeEnabled()
        blacklist_settings_button.visibility = if (overlayEnabled) VISIBLE else GONE
    }

    @SuppressLint("CommitPrefEdits")
    private fun setOverlayEnabled(enabled: Boolean) {
        prefManager.setOverlay(enabled)
        Bus.send(InfoPanelPreferenceChanged(enabled = enabled))
    }

    private fun setDarkThemeEnabled(enabled: Boolean) {
        prefManager.setDarkTheme(enabled)

        if (isServiceRunning(InfoPanelDisplayService::class.java)) {
            val service = Intent(applicationContext, InfoPanelDisplayService::class.java)
            stopService(service)
        }
        this.finish()
        val intent = this.intent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
        this.startActivity(intent)
    }

    @SuppressLint("NewApi")
    private fun shouldLaunchOnboarding(): Boolean {
        val needsPermission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        val hasPermission = try {
            Settings.canDrawOverlays(this)
        } catch (e: NoSuchMethodError) {
            e.printStackTrace()
            true
        }
        val canDrawOverApps = !needsPermission || (needsPermission && hasPermission)
        val serviceIsRunning = isServiceRunning(JapaneseTextGrabberService::class.java)
        return !(canDrawOverApps && serviceIsRunning)
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
