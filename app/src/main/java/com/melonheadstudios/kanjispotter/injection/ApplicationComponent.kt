package com.melonheadstudios.kanjispotter.injection

import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import dagger.Component
import javax.inject.Singleton

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(application: MainApplication)
    fun inject(textGrabberService: JapaneseTextGrabberService)
}