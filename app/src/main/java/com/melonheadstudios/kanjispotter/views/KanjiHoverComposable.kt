package com.melonheadstudios.kanjispotter.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.extensions.horizontalFadingEdge
import com.melonheadstudios.kanjispotter.extensions.verticalFadingEdge
import com.melonheadstudios.kanjispotter.models.Kanji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

@Composable
fun Clip(text: String, isSelected: Boolean, onClicked: () -> Unit) {
    val color = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.2f)
    val shape = RoundedCornerShape(18.dp)
    Surface(shape = shape,
            modifier = Modifier
                    .border(width = 1.dp, color = color, shape = shape)
                    .clip(shape = shape)
                    .clickable { onClicked() }) {
        Text(text = text,
                style = MaterialTheme.typography.subtitle2,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                        .defaultMinSize(minWidth = 44.dp)
                        .padding(8.dp)
        )
    }
}


@Composable
fun ShowAllClip(allOn: Boolean, onClicked: () -> Unit) {
    Clip(text = if (allOn) "Show None" else "Show All", isSelected = allOn, onClicked = onClicked)
}


@Composable
fun KanjiClip(kanji: Kanji, isSelected: Boolean, onClicked: () -> Unit) {
    Clip(kanji.baseForm, isSelected, onClicked)
}

@Composable
fun KanjiSelection(kanjiList: Set<Kanji>, filteredKanji: Set<Kanji>, selectAll: () -> Unit, onFilterToggled: (kanji: Kanji) -> Unit, ) {
    val scrollState = rememberScrollState()
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                    .fillMaxWidth()
                    .horizontalFadingEdge(scrollState, length = 150.dp)
                    .horizontalScroll(scrollState)
                    .padding(bottom = 10.dp)
    ) {
        ShowAllClip(onClicked = selectAll, allOn = filteredKanji.isEmpty())
        kanjiList.forEach { kanji ->
            KanjiClip(kanji = kanji, isSelected = !filteredKanji.contains(kanji), onClicked = {
                onFilterToggled(kanji)
            })
        }
    }
}

@Composable
fun KanjiEntry(kanji: Kanji) {
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


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KanjiHoverDisplay(parsedKanji: Set<Kanji>, filteredKanji: Set<Kanji>, showAllClicked: () -> Unit, onFilterToggled: (kanji: Kanji) -> Unit) {
    Box(modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = RoundedCornerShape(32.dp))
            .padding(24.dp)) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.fillMaxWidth()) {
            KanjiSelection(parsedKanji, filteredKanji, showAllClicked, onFilterToggled)
            Column(Modifier
                    .verticalFadingEdge(scrollState, length = 150.dp)
                    .verticalScroll(scrollState)
                    .fillMaxWidth()) {
                AnimatedContent(targetState = parsedKanji.filter { !filteredKanji.contains(it) }) { validKanji ->
                    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        for (kanji in validKanji) {
                            KanjiEntry(kanji = kanji)
                        }
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
            Kanji(baseForm = "主人", reading = "シュジン", Date(), async { return@async "Husband" }),
            Kanji(baseForm = "主人1", reading = "シュジン", Date(), async { return@async "Husband" }),
            Kanji(baseForm = "主人2", reading = "シュジン", Date(), async { return@async "Husband" }),
        )
    }
    KanjiHoverDisplay(kanji, setOf(kanji.first()), { }, { })
}