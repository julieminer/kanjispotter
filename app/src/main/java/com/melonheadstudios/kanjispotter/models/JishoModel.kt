package com.melonheadstudios.kanjispotter.models

import kotlinx.serialization.Serializable

/**
 * kanjispotter
 * Created by jake on 2018-04-29, 12:25 PM
 */

@Serializable
data class JishoModel(
    val meta: Meta?,
    val data: List<Data>?
)

@Serializable
data class Meta(
    val status: Int?
)

@Serializable
data class Data(
    val is_common: Boolean?,
    val tags: List<String>?,
    val japanese: List<Japanese>?,
    val senses: List<Sense>?
)

@Serializable
data class Japanese(
    val word: String?,
    val reading: String?
)

@Serializable
data class Sense(
    val english_definitions: List<String>?,
    val parts_of_speech: List<String>?
)

fun JishoModel.englishDefinition(): String? = data?.first()?.senses?.first()?.english_definitions?.joinToString { it }