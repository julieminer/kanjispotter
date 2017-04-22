package com.melonheadstudios.kanjispotter.injection

import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.activities.MainActivity
import com.melonheadstudios.kanjispotter.services.InfoPanelDisplayService
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.services.QuickTileService
import dagger.Component
import javax.inject.Singleton

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(application: MainApplication)
    fun inject(textGrabberService: JapaneseTextGrabberService)
    fun inject(infoPanelDisplayService: InfoPanelDisplayService)
    fun inject(tileService: QuickTileService)
}