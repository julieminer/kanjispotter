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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.OnboardingScreen
import com.melonheadstudios.kanjispotter.repos.OnboardingRepo
import org.koin.androidx.compose.get

@Composable
fun Onboarding(onboardingScreen: OnboardingScreen, onboardingRepo: OnboardingRepo = get()) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = onboardingScreen.title, style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface, modifier = Modifier.fillMaxWidth())
        Text(text = onboardingScreen.description, color = MaterialTheme.colors.onSurface, style = MaterialTheme.typography.caption)
        Image(painter = painterResource(id = onboardingScreen.imageId),
            contentDescription = onboardingScreen.imageContentDescription,
            Modifier.weight(1f).padding(vertical = 15.dp))
        Button(onClick = { onboardingRepo.startAction(context, onboardingScreen) }) {
            Text(text = onboardingScreen.actionTitle)
        }
    }
}

@Preview
@Composable
fun PreviewOnboarding() {
    Onboarding(onboardingScreen = OnboardingScreen.Accessibility)
}