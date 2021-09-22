package com.melonheadstudios.kanjispotter

import android.app.Application
import com.google.firebase.FirebaseApp
import com.melonheadstudios.kanjispotter.utils.NotificationManager
import com.melonheadstudios.kanjispotter.injection.appModule
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.services.PreferencesService
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * MainApplication
 * Created by jake on 2017-04-15, 9:40 AM
 */
class MainApplication: Application() {
    val kanjiRepo: KanjiRepo by inject()
    val preferencesService: PreferencesService by inject()
    val scope: CoroutineScope by inject()
    private val notificationManager: NotificationManager by inject()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }

        instance = this
        // force initialization
        notificationManager
    }

    companion object {
        lateinit var instance: MainApplication
    }
}