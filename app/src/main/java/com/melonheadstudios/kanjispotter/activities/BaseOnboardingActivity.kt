package com.melonheadstudios.kanjispotter.activities

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 5:55 PM
 */

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.github.jrejaud.viewpagerindicator2.CirclePageIndicator
import com.jrejaud.onboarder.OnboardingFragment
import com.jrejaud.onboarder.OnboardingFragment.onOnboardingButtonClickListener
import com.jrejaud.onboarder.OnboardingPage
import com.jrejaud.onboarder.R.id
import com.jrejaud.onboarder.R.layout
import java.io.Serializable

open class BaseOnboardingActivity : AppCompatActivity(), onOnboardingButtonClickListener {
    private var onboardingPages: List<OnboardingPage>? = null
    private var onboardingFragmentPagerAdapter: BaseOnboardingActivity.OnboardingFragmentPagerAdapter? = null
    private var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(layout.activity_onboarding)
        this.supportActionBar?.hide()
        val bundle = this.intent.extras
        val swipingEnabled = bundle.getBoolean("SWIPING_ENABLED", true)
        val hideDotPagination = bundle.getBoolean("HIDE_DOT_PAGINATION", false)
        val backgroundImageResId = bundle.getInt("BACKGROUND_IMAGE_RES_ID", -1)
        val backgroundColorResId = bundle.getInt("BACKGROUND_COLOR_RES_ID", -1)
        this.onboardingPages = bundle.getSerializable("ONBOARDING_FRAGMENT_LIST") as List<OnboardingPage>
        this.viewPager = this.findViewById(id.onboarding_viewpager) as ViewPager
        var circlePageIndicator = this.findViewById(id.onboadring_page_indicator) as CirclePageIndicator
        this.onboardingFragmentPagerAdapter = OnboardingFragmentPagerAdapter(this.supportFragmentManager)
        if (swipingEnabled) {
            this.viewPager = this.findViewById(id.onboarding_viewpager) as ViewPager
            this.viewPager!!.adapter = this.onboardingFragmentPagerAdapter
            circlePageIndicator = this.findViewById(id.onboadring_page_indicator) as CirclePageIndicator
            if (!hideDotPagination) {
                circlePageIndicator.setViewPager(this.viewPager)
            } else {
                circlePageIndicator.visibility = View.GONE
            }
        } else {
            this.viewPager!!.visibility = View.GONE
            circlePageIndicator.visibility = View.GONE
            val onboardingBackground = this.supportFragmentManager.beginTransaction()
            onboardingBackground.add(id.onboarding_layout, this.onboardingFragmentPagerAdapter!!.getItem(0))
            onboardingBackground.setTransition(4097)
            onboardingBackground.commit()
        }

        val onboardingBackground1 = this.findViewById(id.onboarding_background_image) as ImageView
        if (backgroundImageResId != -1) {
            onboardingBackground1.setImageResource(backgroundImageResId)
        } else if (backgroundColorResId != -1) {
            this.window.decorView.setBackgroundColor(this.resources.getColor(backgroundColorResId))
        }

    }

    fun goToNextFragment(currentPosition: Int) {
        if (currentPosition + 1 >= this.onboardingFragmentPagerAdapter!!.count) {
            this.finish()
        } else {
            this.viewPager!!.post({
                onboardingFragmentPagerAdapter?.notifyDataSetChanged()
                viewPager!!.setCurrentItem(currentPosition + 1, false)
            })
        }
    }

    override fun onOnboardingClick(position: Int) {
        this.goToNextFragment(position)
        Log.e(com.jrejaud.onboarder.OnboardingActivity::class.java.simpleName, "You need to extend Onboarding Activity and override onOnboardingClick to make it do something besides move to the next fragment and finish the onboarding when its on the last fragment.")
    }

    private inner class OnboardingFragmentPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return OnboardingFragment.newInstance(this@BaseOnboardingActivity.onboardingPages!![position], position)
        }

        override fun getCount(): Int {
            return this@BaseOnboardingActivity.onboardingPages!!.size
        }
    }

    companion object {
        private val BACKGROUND_IMAGE_RES_ID = "BACKGROUND_IMAGE_RES_ID"
        private val BACKGROUND_COLOR_RES_ID = "BACKGROUND_COLOR_RES_ID"
        private val ONBOARDING_FRAGMENT_LIST = "ONBOARDING_FRAGMENT_LIST"
        val SWIPING_ENABLED = "SWIPING_ENABLED"
        val HIDE_DOT_PAGINATION = "HIDE_DOT_PAGINATION"

        fun newBundleImageBackground(@DrawableRes backgroundImageResId: Int, onboardingPages: List<OnboardingPage>): Bundle {
            val bundle = Bundle()
            bundle.putInt("BACKGROUND_IMAGE_RES_ID", backgroundImageResId)
            bundle.putSerializable("ONBOARDING_FRAGMENT_LIST", onboardingPages as Serializable)
            return bundle
        }

        fun newBundleColorBackground(@ColorRes backgroundColorResId: Int, onboardingPages: List<OnboardingPage>): Bundle {
            val bundle = Bundle()
            bundle.putInt("BACKGROUND_COLOR_RES_ID", backgroundColorResId)
            bundle.putSerializable("ONBOARDING_FRAGMENT_LIST", onboardingPages as Serializable)
            return bundle
        }
    }
}

