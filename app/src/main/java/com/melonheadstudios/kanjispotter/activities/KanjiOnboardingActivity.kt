package com.melonheadstudios.kanjispotter.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.activities.fragments.OnboardingFragment
import com.melonheadstudios.kanjispotter.activities.fragments.OnboardingFragmentListener
import com.melonheadstudios.kanjispotter.extensions.canDrawOverlays
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.viewmodels.OnboardingViewModel
import kotlinx.android.synthetic.main.activity_onboarding.*

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 5:23 PM
 */
class KanjiOnboardingActivity: AppCompatActivity(), OnboardingFragmentListener {
    val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE: Int = 5469
    val ACTION_ACESSIBILITY_REQUEST_CODE: Int = 5269

    var userSettingOverlay = false
    var userSettingAccessibility = false

    val pages = arrayOf( OnboardingFragment(), OnboardingFragment(), OnboardingFragment() )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        pages.forEachIndexed { index, page -> page.setupPage(this, index, viewModelForPage(index)) }

        goToPage(0, false)
    }

    private fun viewModelForPage(index: Int): OnboardingViewModel {
        return when (index) {
            0 -> OnboardingViewModel(R.drawable.image1b, R.string.page_1_text, R.string.page_1_desc, R.string.page_1_button)
            1 -> OnboardingViewModel(R.drawable.image2b, R.string.page_2_text, R.string.page_2_desc, R.string.page_2_button)
            2 -> OnboardingViewModel(R.drawable.image3, R.string.page_3_text, R.string.page_3_desc, R.string.page_3_button)
            else -> OnboardingViewModel()
        }
    }

    private fun goToPage(pageIndex: Int, withTransition: Boolean = true) {
        oboarding_content_layout.post {
            var transaction = supportFragmentManager.beginTransaction()
            if (withTransition) {
                transaction = transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.oboarding_content_layout, pages[pageIndex])
            } else {
                transaction = transaction.add(R.id.oboarding_content_layout, pages[pageIndex])
            }
            transaction.commitNow()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }

    override fun onPageButtonClicked(pageNumber: Int) {
        when (pageNumber) {
            0 -> {
                if (!canDrawOverlays()) {
                    userSettingOverlay = true
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                } else {
                    goToPage(1)
                }
            }
            1 -> {
                if (!isMyServiceRunning(JapaneseTextGrabberService::class.java)) {
                    userSettingAccessibility = true
                    val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivityForResult(intent, ACTION_ACESSIBILITY_REQUEST_CODE)
                } else {
                    goToPage(2)
                }
            }
            2 -> {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (this.canDrawOverlays()) {
                goToPage(1)
            }
        } else if (requestCode == ACTION_ACESSIBILITY_REQUEST_CODE) {
            if (isMyServiceRunning(JapaneseTextGrabberService::class.java)) {
                goToPage(2)
            }
        }
    }
}