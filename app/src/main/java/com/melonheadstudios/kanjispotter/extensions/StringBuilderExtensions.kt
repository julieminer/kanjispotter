package com.melonheadstudios.kanjispotter.extensions

/**
 * KanjiSpotter
 * Created by jake on 2017-04-15, 10:23 AM
 */
fun StringBuilder.stringify(): String {
    return this.toString().trimReduce()
}