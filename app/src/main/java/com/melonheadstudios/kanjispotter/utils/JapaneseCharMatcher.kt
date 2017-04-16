package com.melonheadstudios.kanjispotter.utils

import java.lang.Character.UnicodeBlock.*

/**
 * JapaneseCharMatcher
 * Created by jake on 2017-04-14, 11:21 PM
 */

object JapaneseCharMatcher {
    fun isJapanese(char: Char): Boolean {
        val temp = Character.UnicodeBlock.of(char)
        return  temp == CJK_UNIFIED_IDEOGRAPHS || temp == HIRAGANA || temp == KATAKANA ||
        temp == HALFWIDTH_AND_FULLWIDTH_FORMS || temp == HALFWIDTH_AND_FULLWIDTH_FORMS ||
        temp == CJK_SYMBOLS_AND_PUNCTUATION
    }

    fun isKanji(char: Char): Boolean {
        return Character.UnicodeBlock.of(char) == CJK_UNIFIED_IDEOGRAPHS
    }
}
