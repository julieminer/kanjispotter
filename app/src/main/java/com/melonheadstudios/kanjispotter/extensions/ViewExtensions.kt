package com.melonheadstudios.kanjispotter.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * kanjispotter
 * Created by jake on 2017-04-16, 3:50 PM
 */
fun View.saveToClipboard(text: String) {
    this.setOnLongClickListener { v ->
        val context = v.context
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("KanjiSpotter", text)
        clipboard.primaryClip = clip
        Toast.makeText(context, "Copied ${text} to clipboard", Toast.LENGTH_SHORT).show()
        true
    }
}
