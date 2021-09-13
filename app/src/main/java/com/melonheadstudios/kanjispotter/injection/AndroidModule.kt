package com.melonheadstudios.kanjispotter.injection

import com.atilika.kuromoji.ipadic.Tokenizer
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.models.ApplicationJsonAdapterFactory
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.squareup.moshi.Moshi
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module


/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
val appModule = module {
    single(named("appContext")) { androidContext() }
    single { MainThreadBus() }
    single { PrefManager(get()) }
    single { Tokenizer() }
    single { KanjiRepo(get(), get()) }
    single { Moshi.Builder().add(ApplicationJsonAdapterFactory.INSTANCE).build() }
}