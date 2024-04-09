package com.forkbombsquad.stillalivelarp.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import com.forkbombsquad.stillalivelarp.NoStatusBarActivity

class StillAliveLarpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StillAliveLarpApplication.context = applicationContext
    }

    companion object {
        lateinit var context: Context
        lateinit var activity: NoStatusBarActivity
    }

}