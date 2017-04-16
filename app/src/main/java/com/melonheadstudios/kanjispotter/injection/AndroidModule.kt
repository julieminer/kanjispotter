package com.melonheadstudios.kanjispotter.injection

import android.app.Application
import android.content.Context
import com.melonheadstudios.kanjispotter.managers.TextManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
@Module
class AndroidModule(private val application: Application) {

    /**
     * Allow the application context to be injected but require that it be annotated with [ ][ForApplication] to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    @ForApplication
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun providesTextManager(): TextManager {
        return TextManager(application)
    }

//    @Provides
//    @Singleton
//    @Named("something")
//    fun provideSomething(): String {
//        return "something"
//    }
}