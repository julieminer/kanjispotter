package com.melonheadstudios.kanjispotter.extensions

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp

// Source: https://gist.github.com/dovahkiin98/85acb72ab0c4ddfc6b53413c955bcd10

fun Modifier.horizontalFadingEdge(
    scrollState: ScrollState,
    length: Dp,
    edgeColor: Color? = null,
) = composed(
    debugInspectorInfo {
        name = "length"
        value = length
    }
) {
    val color = edgeColor ?: MaterialTheme.colors.surface

    Modifier.drawWithContent {
        val lengthValue = length.toPx()
        val scrollFromStart = scrollState.value
        val scrollFromEnd = scrollState.maxValue - scrollState.value
        val startFadingEdgeStrength = lengthValue * (scrollFromStart / lengthValue).coerceAtMost(1f)
        val endFadingEdgeStrength = lengthValue * (scrollFromEnd / lengthValue).coerceAtMost(1f)
        drawContent()

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                        color,
                        Color.Transparent,
                ),
                startX = 0f,
                endX = startFadingEdgeStrength,
            ),
            size = Size(
                startFadingEdgeStrength,
                this.size.height,
            ),
        )

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color,
                ),
                startX = size.width - endFadingEdgeStrength,
                endX = size.width,
            ),
            topLeft = Offset(x = size.width - endFadingEdgeStrength, y = 0f),
        )
    }
}

fun Modifier.verticalFadingEdge(
    scrollState: ScrollState,
    length: Dp,
    edgeColor: Color? = null,
) = composed(
    debugInspectorInfo {
        name = "length"
        value = length
    }
) {
    val color = edgeColor ?: MaterialTheme.colors.surface

    Modifier.drawWithContent {
        val lengthValue = length.toPx()
        val scrollFromTop = scrollState.value
        val scrollFromBottom = scrollState.maxValue - scrollState.value
        val topFadingEdgeStrength = lengthValue * (scrollFromTop / lengthValue).coerceAtMost(1f)
        val bottomFadingEdgeStrength = lengthValue * (scrollFromBottom / lengthValue).coerceAtMost(1f)

        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color,
                    Color.Transparent,
                ),
                startY = 0f,
                endY = topFadingEdgeStrength,
            ),
            size = Size(
                this.size.width,
                topFadingEdgeStrength
            ),
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                        Color.Transparent,
                        color,
                ),
                startY = size.height - bottomFadingEdgeStrength,
                endY = size.height,
            ),
            topLeft = Offset(x = 0f, y = size.height - bottomFadingEdgeStrength),
        )
    }
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.verticalFadingEdge(
    scrollState: LazyListState,
    length: Dp,
    edgeColor: Color? = null,
) = composed(
    debugInspectorInfo {
        name = "length"
        value = length
    }
) {
    val color = edgeColor ?: MaterialTheme.colors.surface
    val height = remember { mutableStateOf(0) }

    Modifier.onSizeChanged { size -> height.value = size.height }.drawWithContent {
        if (scrollState.layoutInfo.totalItemsCount != 0) {
            val lengthValue = length.toPx()
            val itemSize = scrollState.layoutInfo.visibleItemsInfo.first().size
            val totalItems = scrollState.layoutInfo.totalItemsCount
            val totalSize = itemSize * totalItems
            val scrolledTopPosition = ((scrollState.firstVisibleItemIndex) * itemSize) + scrollState.firstVisibleItemScrollOffset
            val scrolledBottomPosition = scrolledTopPosition + height.value
            val scrollFromBottom = totalSize - scrolledBottomPosition
            val topFadingEdgeStrength = lengthValue * (scrolledTopPosition / lengthValue).coerceAtMost(1f)
            val bottomFadingEdgeStrength = lengthValue * (scrollFromBottom / lengthValue).coerceAtMost(1f)

            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color,
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = topFadingEdgeStrength,
                ),
                size = Size(
                    this.size.width,
                    topFadingEdgeStrength
                ),
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color,
                    ),
                    startY = size.height - bottomFadingEdgeStrength,
                    endY = size.height,
                ),
                topLeft = Offset(x = 0f, y = size.height - bottomFadingEdgeStrength),
            )
        }
    }
}