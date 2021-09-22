package com.melonheadstudios.kanjispotter.services

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.melonheadstudios.kanjispotter.models.JishoModel
import com.melonheadstudios.kanjispotter.models.englishDefinition
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

class JishoService {
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val cache: HashMap<String, String> = hashMapOf()

    suspend fun get(kanji: String): String? {
        if (cache.containsKey(kanji)) {
            return cache[kanji]
        }

        return try {
            val jishoModel: JishoModel? = client.get("http://jisho.org/api/v1/search/words?keyword=$kanji")
            val english = jishoModel?.englishDefinition()
            english?.let { cache[kanji] = it }
            return english
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
            null
        }
    }
}