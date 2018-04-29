package com.melonheadstudios.kanjispotter.models

import se.ansman.kotshi.JsonSerializable

/**
 * kanjispotter
 * Created by jake on 2018-04-29, 12:25 PM
 */

@JsonSerializable
data class JishoModel(
    val meta: Meta,
    val data: List<Data>
)

@JsonSerializable
data class Meta(
    val status: Int
)

@JsonSerializable
data class Data(
    val is_common: Boolean,
    val tags: List<String>,
    val japanese: List<Japanese>,
    val senses: List<Sense>,
    val attribution: Attribution
)

@JsonSerializable
data class Japanese(
    val word: String,
    val reading: String
)

@JsonSerializable
data class Sense(
    val english_definitions: List<String>,
    val parts_of_speech: List<String>,
    val links: List<Any>,
    val tags: List<Any>,
    val restrictions: List<Any>,
    val see_also: List<Any>,
    val antonyms: List<Any>,
    val source: List<Any>,
    val info: List<Any>
)

@JsonSerializable
data class Attribution(
    val jmdict: Boolean,
    val jmnedict: Boolean,
    val dbpedia: Boolean
)

fun JishoModel.englishDefinition(): String? = data.first()?.senses?.first()?.english_definitions?.joinToString { it }