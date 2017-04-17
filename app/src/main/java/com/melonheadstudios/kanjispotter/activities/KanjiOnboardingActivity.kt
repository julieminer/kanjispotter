package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService


/**
 * kanjispotter
 * Created by jake on 2017-04-16, 5:23 PM
 */
class KanjiOnboardingActivity: BaseOnboardingActivity() {
    val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE: Int = 5469
    val ACTION_ACESSIBILITY_REQUEST_CODE: Int = 5269

    var userSettingOverlay = false
    var userSettingAccessibility = false
    override fun onOnboardingClick(position: Int) {
        when (position) {
            0 -> {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    userSettingOverlay = true
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                } else {
                    goToNextFragment(position)
                }
            }
            1 -> {
                if (!isMyServiceRunning(JapaneseTextGrabberService::class.java)) {
                    userSettingAccessibility = true
                    val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivityForResult(intent, ACTION_ACESSIBILITY_REQUEST_CODE)
                } else {
                    goToNextFragment(position)
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        if (userSettingOverlay && !userSettingAccessibility) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                // user can move on
                goToNextFragment(0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                goToNextFragment(1)
            }
        } else if (requestCode == ACTION_ACESSIBILITY_REQUEST_CODE) {
            if (isMyServiceRunning(JapaneseTextGrabberService::class.java)) {
                goToNextFragment(2)
            } else {

            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }
}