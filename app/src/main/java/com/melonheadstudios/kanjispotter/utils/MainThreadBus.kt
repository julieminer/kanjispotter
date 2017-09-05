package com.melonheadstudios.kanjispotter.utils

import android.os.Handler
import android.os.Looper
import com.squareup.otto.Bus



/**
 * kanjispotter
 * Created by jake on 2017-09-04, 10:36 PM
 */
class MainThreadBus : Bus() {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun post(event: Any) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event)
        } else {
            mHandler.post { super@MainThreadBus.post(event) }
        }
    }
}