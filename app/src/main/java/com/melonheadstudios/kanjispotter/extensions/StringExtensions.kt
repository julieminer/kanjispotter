package com.melonheadstudios.kanjispotter.extensions

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:23 AM
 */
fun String.trimReduce(): String {
    return this.trim().replace("\\s+".toRegex(), " ")
}

fun String.getFurigana(furigana: ((String) -> Unit)) {
    val dir = "https://tatoeba.org/eng/tools/furigana?query="
    (dir + this).httpGet().responseString { _, _, result ->
        //do something with response
        when (result) {
            is Result.Failure -> {
                furigana("")
            }
            is Result.Success -> {
                val data = result.get()
                furigana(data)
            }
        }
    }
}

fun String.getReadings(readings: ((String) -> Unit)) {
    val dir = "http://jisho.org/api/v1/search/words?keyword="
    (dir + this).httpGet().responseString { _, _, result ->
        //do something with response
        when (result) {
            is Result.Failure -> {
                readings("")
            }
            is Result.Success -> {
                val data = result.get()
                readings(data)
            }
        }
    }
}
