package com.melonheadstudios.kanjispotter.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.melonheadstudios.kanjispotter.R
import com.jrejaud.onboarder.OnboardingPage
import java.util.*
import com.jrejaud.onboarder.OnboardingActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
