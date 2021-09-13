package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.IABUpdateUIEvent
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.services.HoverPanelService
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val prefManager: PrefManager by inject()
    private val bus: MainThreadBus by inject()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        debug_button.visibility = if (BuildConfig.DEBUG) VISIBLE else GONE

        if (shouldLaunchOnboarding()) {
            startActivity(Intent(this, KanjiOnboardingActivity::class.java))
        }

        report_issue_button.setOnClickListener {
            reportIssue()
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

        debug_button.setOnClickListener {
            val startHoverIntent = Intent(this, HoverPanelService::class.java)
            startService(startHoverIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    private fun updateUI() {
        val overlayEnabled = prefManager.overlayEnabled()
        spotter_overlay_switch.isChecked = overlayEnabled
        theme_dark_switch.isChecked = prefManager.darkThemeEnabled()
        blacklist_settings_button.visibility = if (overlayEnabled) VISIBLE else GONE
    }

    @SuppressLint("CommitPrefEdits")
    private fun setOverlayEnabled(enabled: Boolean) {
        prefManager.setOverlay(enabled)
        bus.post(InfoPanelPreferenceChanged(enabled = enabled))
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WrongConstant")
    private fun setDarkThemeEnabled(enabled: Boolean) {
        prefManager.setDarkTheme(enabled)

        if (isServiceRunning(HoverPanelService::class.java)) {
            val service = Intent(applicationContext, HoverPanelService::class.java)
            stopService(service)
        }
        this.finish()
        val intent = this.intent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

    @Subscribe
    fun onInfoPanelEvent(e: InfoPanelPreferenceChanged) {
        updateUI()
    }

    @Subscribe
    fun onIABUpdateEvent(e: IABUpdateUIEvent) {
        updateUI()
    }
}
