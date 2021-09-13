package com.melonheadstudios.kanjispotter.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.melonheadstudios.kanjispotter.utils.Constants

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 3:50 PM
 */
fun View.saveToClipboard(text: String) {
    this.setOnLongClickListener { v ->
        val context = v.context
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("KanjiSpotter", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $text to clipboard", Toast.LENGTH_SHORT).show()
        FirebaseAnalytics.getInstance(context).logEvent(Constants.EVENT_CLIPBOARD, Bundle())
        true
    }
}

