package com.melonheadstudios.kanjispotter.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.viewmodels.OnboardingViewModel
import kotlinx.android.synthetic.main.oboarding_page.*

/**
 * kanjispotter
 * Created by jake on 2017-04-18, 8:13 PM
 */
class OnboardingFragment: Fragment() {
    private lateinit var delegate: OnboardingFragmentListener
    private var viewModel: OnboardingViewModel? = null
    private var pageNumber: Int? = null
    private var viewCreated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.oboarding_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewCreated = true
        updatePage(viewModel ?: return)
    }

    private fun updatePage(viewModel: OnboardingViewModel) {
        if (!viewCreated) return
        onboarding_title.setText(viewModel.titleText!!)
        onboarding_description.setText(viewModel.descriptionText!!)
        onboarding_button.setText(viewModel.buttonText!!)
        onboarding_image.setImageResource(viewModel.image!!)

        onboarding_button.setOnClickListener {
            delegate.onPageButtonClicked(pageNumber!!)
        }
    }

    fun setupPage(listener: OnboardingFragmentListener, index: Int, model: OnboardingViewModel) {
        delegate = listener
        pageNumber = index
        viewModel = model
        updatePage(viewModel!!)
    }
}

interface OnboardingFragmentListener {
    fun onPageButtonClicked(pageNumber: Int)
}