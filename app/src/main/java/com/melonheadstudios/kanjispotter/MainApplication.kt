package com.melonheadstudios.kanjispotter

import android.app.Application
import com.melonheadstudios.kanjispotter.injection.appModule
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.services.JishoService
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * MainApplication
 * Created by jake on 2017-04-15, 9:40 AM
 */
class MainApplication: Application() {
    val bus: MainThreadBus by inject()
    val prefManager: PrefManager by inject()
    val kanjiRepo: KanjiRepo by inject()
    val jishoService: JishoService by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }

        instance = this
    }

    companion object {
        lateinit var instance: MainApplication
    }
}