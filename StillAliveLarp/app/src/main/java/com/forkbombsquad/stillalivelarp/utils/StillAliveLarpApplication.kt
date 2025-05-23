package com.forkbombsquad.stillalivelarp.utils

import android.app.Application
import android.content.Context
import com.forkbombsquad.stillalivelarp.NoStatusBarActivity
import java.lang.ref.WeakReference

class StillAliveLarpApplication : Application() {

    companion object {
        private var currentActivtyRef: WeakReference<NoStatusBarActivity>? = null

        val currentActivty: NoStatusBarActivity?
            get() = currentActivtyRef?.get()

        fun setCurrentActivty(activity: NoStatusBarActivity) {
            currentActivtyRef = WeakReference(activity)
        }
    }

}