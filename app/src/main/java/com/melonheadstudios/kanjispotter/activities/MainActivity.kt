package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.utils.NotificationManager
import com.melonheadstudios.kanjispotter.views.MainScreen
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val manager: NotificationManager by inject()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContent {
            MainScreen()
        }

        if (shouldLaunchOnboarding()) {
            startActivity(Intent(this, KanjiOnboardingActivity::class.java))
        }
//
//        report_issue_button.setOnClickListener {
//            reportIssue()
//        }

    }

    @SuppressLint("NewApi")
    private fun shouldLaunchOnboarding(): Boolean {
        val serviceIsRunning = isServiceRunning(JapaneseTextGrabberService::class.java)
        return !(manager.canBubble() && serviceIsRunning)
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
