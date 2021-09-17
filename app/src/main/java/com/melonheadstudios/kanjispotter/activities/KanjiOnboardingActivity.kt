package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.activities.fragments.NotificationHelper
import com.melonheadstudios.kanjispotter.activities.fragments.OnboardingFragment
import com.melonheadstudios.kanjispotter.activities.fragments.OnboardingFragmentListener
import com.melonheadstudios.kanjispotter.extensions.isServiceRunning
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.viewmodels.OnboardingViewModel
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.koin.android.ext.android.inject

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 5:23 PM
 */
class KanjiOnboardingActivity: AppCompatActivity(), OnboardingFragmentListener {
    private val ACTION_BUBBLE_REQUEST_CODE: Int = 5469
    private val ACTION_ACESSIBILITY_REQUEST_CODE: Int = 5269

    private var userSettingAccessibility = false

    private val pages = arrayOf( OnboardingFragment(), OnboardingFragment(), OnboardingFragment() )
    private val helper: NotificationHelper by inject()

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
            transaction = if (withTransition) {
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.oboarding_content_layout, pages[pageIndex])
            } else {
                transaction.add(R.id.oboarding_content_layout, pages[pageIndex])
            }
            transaction.commitNow()
        }
    }

    @SuppressLint("InlinedApi")
    override fun onPageButtonClicked(pageNumber: Int) {
        when (pageNumber) {
            0 -> {
                if (!helper.canBubble()) {
                    startActivityForResult(Intent(Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName), ACTION_BUBBLE_REQUEST_CODE)
                } else {
                    goToPage(1)
                }
            }
            1 -> {
                if (!isServiceRunning(JapaneseTextGrabberService::class.java)) {
                    userSettingAccessibility = true
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_BUBBLE_REQUEST_CODE) {
            if (helper.canBubble()) {
                goToPage(1)
            }
        } else if (requestCode == ACTION_ACESSIBILITY_REQUEST_CODE) {
            if (isServiceRunning(JapaneseTextGrabberService::class.java)) {
                goToPage(2)
            }
        }
    }
}