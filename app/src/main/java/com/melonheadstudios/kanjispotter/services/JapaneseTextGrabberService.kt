// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.melonheadstudios.kanjispotter.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.injection.AndroidModule
import com.melonheadstudios.kanjispotter.injection.DaggerApplicationComponent
import com.melonheadstudios.kanjispotter.managers.TextManager
import javax.inject.Inject


class JapaneseTextGrabberService : AccessibilityService() {
    private val TAG = "JapaneseTextGrabber"

    @Inject
    lateinit var textManager: TextManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        MainApplication.graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(application)).build()
        MainApplication.graph.inject(this)

        Log.d(TAG, "Service connected")

        val info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        textManager.parseEvent(event)
    }

    override fun onInterrupt() {
    }
}
