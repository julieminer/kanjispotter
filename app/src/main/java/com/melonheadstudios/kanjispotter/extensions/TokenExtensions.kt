package com.melonheadstudios.kanjispotter.extensions

import com.atilika.kuromoji.ipadic.Token
import com.melonheadstudios.kanjispotter.models.Kanji
import com.melonheadstudios.kanjispotter.models.englishDefinition
import com.melonheadstudios.kanjispotter.services.JishoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.util.*

fun Token.toKanji(scope: CoroutineScope, jishoService: JishoService): Kanji {
    return Kanji(baseForm.trim(), reading.trim(), Date(), scope.async { jishoService.get(baseForm)?.englishDefinition() } )
}