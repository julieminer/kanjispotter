package com.melonheadstudios.kanjispotter.models

import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.KotshiJsonAdapterFactory

/**
 * kanjispotter
 * Created by jake on 2018-04-29, 12:50 PM
 */
@KotshiJsonAdapterFactory
abstract class ApplicationJsonAdapterFactory : JsonAdapter.Factory {
    companion object {
        val INSTANCE: ApplicationJsonAdapterFactory = KotshiApplicationJsonAdapterFactory()
    }
}