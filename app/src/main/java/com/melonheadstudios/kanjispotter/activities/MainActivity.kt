package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.activities.fragments.NotificationHelper
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.services.DataStore
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val dataStore: DataStore by inject()
    private val helper: NotificationHelper by inject()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        val darkThemeEnabled = runBlocking { dataStore.darkThemeEnabled.firstOrNull() == true }
        val overlayEnabled = runBlocking { dataStore.overlayEnabled.firstOrNull() == true }
        if (!darkThemeEnabled) {
            setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        debug_button.visibility = if (BuildConfig.DEBUG) VISIBLE else GONE

        if (shouldLaunchOnboarding()) {
            startActivity(Intent(this, KanjiOnboardingActivity::class.java))
        }

        lifecycleScope.launch {
            dataStore.overlayEnabled.collect {
                updateOverlay(it == true)
            }

            dataStore.darkThemeEnabled.collect {
                updateDarkTheme(it == true)
            }
        }

        updateDarkTheme(darkThemeEnabled)
        updateOverlay(overlayEnabled)

        report_issue_button.setOnClickListener {
            reportIssue()
        }

        spotter_overlay_switch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                dataStore.setOverlayEnabled(isChecked)
            }
        }

        theme_dark_switch.setOnCheckedChangeListener {  _, isChecked ->
            setDarkThemeEnabled(isChecked)
        }

//        blacklist_settings_button.setOnClickListener {
//            val blacklistIntent = Intent(this, BlacklistActivity::class.java)
//            startActivity(blacklistIntent)
//        }

        debug_button.setOnClickListener {
        }
    }

    private fun updateOverlay(enabled: Boolean) {
//        blacklist_settings_button.visibility = if (enabled) VISIBLE else GONE
        spotter_overlay_switch.isChecked = enabled
    }

    private fun updateDarkTheme(enabled: Boolean) {
        theme_dark_switch.isChecked = enabled
    }

    private fun setDarkThemeEnabled(enabled: Boolean) = lifecycleScope.launch {
        dataStore.setDarkThemeEnabled(enabled)
        this@MainActivity.finish()
        val intent = this@MainActivity.intent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        this@MainActivity.startActivity(intent)
    }

    @SuppressLint("NewApi")
    private fun shouldLaunchOnboarding(): Boolean {
        val serviceIsRunning = isServiceRunning(JapaneseTextGrabberService::class.java)
        return !(helper.canBubble() && serviceIsRunning)
    }

    private fun reportIssue() {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "melonheadstudiosapps@gmail.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Issue report")
        val userInfo = "Build Number: ${BuildConfig.VERSION_CODE}\n Version: ${BuildConfig.VERSION_NAME}\n " +
                "Model: ${Build.MODEL}\n Manufacturer: ${Build.MANUFACTURER}\n" +
                "API Version: ${Build.VERSION.SDK_INT}\n Android Version ${Build.VERSION.RELEASE}\n "
        emailIntent.putExtra(Intent.EXTRA_TEXT, userInfo)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}
