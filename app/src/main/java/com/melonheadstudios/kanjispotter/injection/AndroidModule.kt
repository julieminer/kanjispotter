package com.melonheadstudios.kanjispotter.injection

import com.atilika.kuromoji.ipadic.Tokenizer
import com.melonheadstudios.kanjispotter.utils.NotificationManager
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.repos.OnboardingRepo
import com.melonheadstudios.kanjispotter.services.JishoService
import com.melonheadstudios.kanjispotter.services.PreferencesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:52 AM
 */
val appModule = module {
    single(named("appContext")) { androidContext() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { Tokenizer() }
    single { KanjiRepo(get(), get(), get(), get(), get()) }
    single { JishoService() }
    single { PreferencesService(get()) }
    single { NotificationManager(get()) }
    single { OnboardingRepo(get(), get(), get()) }
}