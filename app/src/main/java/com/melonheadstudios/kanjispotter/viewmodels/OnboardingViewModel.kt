package com.melonheadstudios.kanjispotter.viewmodels

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * kanjispotter
 * Created by jake on 2017-04-18, 8:38 PM
 */

class OnboardingViewModel(@DrawableRes val image: Int? = null,
                          @StringRes val titleText: Int? = null,
                          @StringRes val descriptionText: Int? = null,
                          @StringRes val buttonText: Int? = null)
