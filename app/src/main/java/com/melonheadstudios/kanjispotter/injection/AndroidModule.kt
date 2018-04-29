package com.melonheadstudios.kanjispotter.injection

import android.app.Application
import android.content.Context
import com.atilika.kuromoji.ipadic.Tokenizer
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.managers.IABManager
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.managers.TextManager
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import javax.inject.Singleton


/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
@Module
class AndroidModule(private val application: Application) {

    private val bus = MainThreadBus()
    private val tokenizer = Tokenizer()

    init {
        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(application, Crashlytics.Builder().core(core).build())
    }

    /**
     * Allow the application context to be injected but require that it be annotated with [ ][ForApplication] to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    @ForApplication
    fun provideApplicationContext(): Context = application

    @Provides
    @Singleton
    fun providesTextManager(): TextManager = TextManager(application)

    @Provides
    @Singleton
    fun providesIABManager(): IABManager = IABManager()

    @Provides
    @Singleton
    fun providesPrefManager(): PrefManager = PrefManager(application)

    @Provides
    @Singleton
    fun providesBus(): MainThreadBus = bus

    @Provides
    @Singleton
    fun providesTokenizer(): Tokenizer = tokenizer

//    @Provides
//    @Singleton
//    @Named("something")
//    fun provideSomething(): String {
//        return "something"
//    }
}