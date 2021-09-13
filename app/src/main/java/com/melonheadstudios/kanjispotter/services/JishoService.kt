package com.melonheadstudios.kanjispotter.services

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.melonheadstudios.kanjispotter.models.JishoModel
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

class JishoService {
    private val client = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun get(kanji: String): JishoModel? {
        return try {
            client.get("http://jisho.org/api/v1/search/words?keyword=$kanji")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
            null
        }
    }
}