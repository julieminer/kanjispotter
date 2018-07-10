package com.melonheadstudios.kanjispotter.injection

import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.activities.BlacklistActivity
import com.melonheadstudios.kanjispotter.activities.MainActivity
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.services.HoverPanelService
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.services.QuickTileService
import com.melonheadstudios.kanjispotter.viewmodels.KanjiListModel
import com.melonheadstudios.kanjispotter.views.HoverMenuScreen
import dagger.Component
import javax.inject.Singleton

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: BlacklistActivity)
    fun inject(application: MainApplication)
    fun inject(textGrabberService: JapaneseTextGrabberService)
    fun inject(hoverPanelService: HoverPanelService)
    fun inject(tileService: QuickTileService)
    fun inject(iabManager: IABManager)
    fun inject(hoverMenuScreen: HoverMenuScreen)
    fun inject(kanjiRepo: KanjiRepo)
    fun inject(viewHolder: KanjiListModel.ViewHolder)
}