package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.models.Kanji

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