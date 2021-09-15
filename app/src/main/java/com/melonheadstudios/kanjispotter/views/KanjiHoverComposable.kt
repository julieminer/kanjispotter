package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.models.KanjiInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

@Composable
fun KanjiSelection(kanjiList: List<KanjiInstance>, selectedKanjiIndex: Int?) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp)
    ) {
        kanjiList.forEachIndexed { index, kanji ->
            Text(text = kanji.baseForm,
                    style = MaterialTheme.typography.subtitle2,
                    color = if (selectedKanjiIndex == index) Color.Green else Color.Black,
                    modifier = Modifier.padding(5.dp)
            )
        }
    }
}

@Composable
fun KanjiEntry(kanji: KanjiInstance) {
    val coroutineScope = rememberCoroutineScope()
    val englishReading = remember { mutableStateOf("Testing 23") }
    SideEffect {
        coroutineScope.launch(Dispatchers.Main) {
            englishReading.value = kanji.englishReading.await() ?: ""
        }
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = kanji.baseForm, style = MaterialTheme.typography.h6)
            Text(text = kanji.reading, style = MaterialTheme.typography.body2)
        }
        Text(text = englishReading.value, style = MaterialTheme.typography.body2)
    }
}


@Composable
fun KanjiHoverDisplay(parsedKanji: List<KanjiInstance>, selectedKanjiIndex: Int?) {
    Box(modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(32.dp))
            .padding(24.dp)) {
        Column {
            KanjiSelection(parsedKanji, selectedKanjiIndex)
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (kanji in parsedKanji) {
                        KanjiEntry(kanji = kanji)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = false, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewKanjiHoverDisplay() {
    val kanji = runBlocking {
        listOf(
                KanjiInstance(baseForm = "主人", reading = "シュジン", Date(), async { return@async "Husband" }),
                KanjiInstance(baseForm = "主人", reading = "シュジン", Date(), async { return@async "Husband" }),
                KanjiInstance(baseForm = "主人", reading = "シュジン", Date(), async { return@async "Husband" }),
        )
    }
    KanjiHoverDisplay(kanji, null)
}