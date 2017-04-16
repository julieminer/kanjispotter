package com.melonheadstudios.kanjispotter.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * GlobalActionBarService
 * Created by jake on 2017-04-15, 9:24 AM
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class JishoResponse(val data: Array<JishoResponseData>?, val name: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JishoResponseData(val isCommon: Boolean?, val japanese: Array<JapaneseResponse>?, val senses: Array<SensesResponse>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JapaneseResponse(val reading: String?, val word: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SensesResponse(val english_definitions: Array<String>?)
