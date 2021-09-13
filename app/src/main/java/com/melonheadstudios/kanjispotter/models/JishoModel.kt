package com.melonheadstudios.kanjispotter.models

import kotlinx.serialization.Serializable

/**
 * kanjispotter
 * Created by jake on 2018-04-29, 12:25 PM
 */

@Serializable
data class JishoModel(
    val meta: Meta? = null,
    val data: List<Data>? = null
)

@Serializable
data class Meta(
    val status: Int? = null
)

@Serializable
data class Data(
    val is_common: Boolean? = null,
    val tags: List<String>? = null,
    val japanese: List<Japanese>? = null,
    val senses: List<Sense>?
)

@Serializable
data class Japanese(
    val word: String? = null,
    val reading: String? = null
)

@Serializable
data class Sense(
    val english_definitions: List<String>? = null,
    val parts_of_speech: List<String>? = null
)

fun JishoModel.englishDefinition(): String? = data?.first()?.senses?.first()?.english_definitions?.joinToString { it }