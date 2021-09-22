package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.R

@Composable
fun Onboarding(title: String, imageId: Int, imageContentDescription: String, description: String, actionTitle: String, onContinueTapped: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface, modifier = Modifier.fillMaxWidth())
        Text(text = description, color = MaterialTheme.colors.onSurface, style = MaterialTheme.typography.caption)
        Image(painter = painterResource(id = imageId),
            contentDescription = imageContentDescription,
            Modifier.weight(1f).padding(vertical = 15.dp))
        Button(onClick = onContinueTapped) {
            Text(text = actionTitle)
        }
    }
}

@Preview
@Composable
fun PreviewOnboarding() {
    Onboarding(title = "Accessibility Feature", description = "Thanks! Now we'll just need to enable the accessibility service that powers the app!", imageContentDescription = "Accessibility Permission Image", imageId = R.drawable.accessibility_settings, actionTitle = "Enable Service", onContinueTapped = {})
}