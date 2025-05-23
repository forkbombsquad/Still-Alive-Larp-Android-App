package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication

open class NoStatusBarActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        StillAliveLarpApplication.setCurrentActivty(this)
    }

}