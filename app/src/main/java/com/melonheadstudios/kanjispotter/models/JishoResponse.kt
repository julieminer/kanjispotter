package com.melonheadstudios.kanjispotter.models

/**
 * GlobalActionBarService
 * Created by jake on 2017-04-15, 9:24 AM
 */

data class JishoResponse(val data: Array<JishoResponseData>?, val name: String?)

data class JishoResponseData(val isCommon: Boolean?, val japanese: Array<JapaneseResponse>?, val senses: Array<SensesResponse>?)

data class JapaneseResponse(val reading: String?, val word: String?)

data class SensesResponse(val english_definitions: Array<String>?)
