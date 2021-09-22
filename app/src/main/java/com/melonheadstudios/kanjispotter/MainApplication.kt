package com.melonheadstudios.kanjispotter

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.FirebaseApp
import com.melonheadstudios.kanjispotter.injection.appModule
import com.melonheadstudios.kanjispotter.repos.OnboardingRepo
import com.melonheadstudios.kanjispotter.utils.NotificationManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * MainApplication
 * Created by jake on 2017-04-15, 9:40 AM
 */
@Suppress("unused")
class MainApplication: Application(), LifecycleObserver {
    private val notificationManager: NotificationManager by inject()
    private val onboardingRepo: OnboardingRepo by inject()

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
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnteredForeground() {
        onboardingRepo.checkPermissions()
    }

    companion object {
        lateinit var instance: MainApplication
    }
}