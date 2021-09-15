package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun KanjiClip(kanji: KanjiInstance, isSelected: Boolean, onClicked: () -> Unit) {
    Text(text = kanji.baseForm,
            style = MaterialTheme.typography.subtitle2,
            color = if (isSelected) Color.Green else Color.Black,
            modifier = Modifier
                    .padding(5.dp)
                    .clickable { onClicked() }
    )
}

@Composable
fun KanjiSelection(kanjiList: Set<KanjiInstance>, filteredKanji: Set<KanjiInstance>, onFilterToggled: (kanji: KanjiInstance) -> Unit, ) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp)
    ) {
        kanjiList.forEach { kanji ->
            KanjiClip(kanji = kanji, isSelected = filteredKanji.contains(kanji), onClicked = {
                onFilterToggled(kanji)
            })
        }
    }
}

@Composable
fun KanjiEntry(kanji: KanjiInstance) {
    val coroutineScope = rememberCoroutineScope()
    val englishReading = remember { mutableStateOf("") }
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
fun KanjiHoverDisplay(parsedKanji: Set<KanjiInstance>, filteredKanji: Set<KanjiInstance>, onFilterToggled: (kanji: KanjiInstance) -> Unit) {
    Box(modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(32.dp))
            .padding(24.dp)) {
        Column {
            KanjiSelection(parsedKanji, filteredKanji, onFilterToggled)
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val validKanji = if (filteredKanji.isEmpty()) parsedKanji else parsedKanji.filter { filteredKanji.contains(it) }
                    for (kanji in validKanji) {
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
        setOf(
                KanjiInstance(baseForm = "主人", reading = "シュジン", Date(), async { return@async "Husband" }),
                KanjiInstance(baseForm = "主人1", reading = "シュジン", Date(), async { return@async "Husband" }),
                KanjiInstance(baseForm = "主人2", reading = "シュジン", Date(), async { return@async "Husband" }),
        )
    }
    KanjiHoverDisplay(kanji, setOf(), { })
}