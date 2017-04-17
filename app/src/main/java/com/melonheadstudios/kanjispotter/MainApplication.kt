package com.melonheadstudios.kanjispotter

import android.app.Application
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.ApplicationComponent
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent

/**
 * MainApplication
 * Created by jake on 2017-04-15, 9:40 AM
 */
class MainApplication: Application() {

    companion object {
        //platformStatic allow access it from java code
        @JvmStatic lateinit var graph: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()
        graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        graph.inject(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}