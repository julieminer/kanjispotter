package com.melonheadstudios.kanjispotter.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.melonheadstudios.kanjispotter.views.HoverScreen

class KanjiBubbleActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HoverScreen() }
    }
}