package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope


class ActivityTemplate : NoStatusBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
        setupView()
    }

    private fun setupView() {

        buildView()
    }

    private fun buildView() {

    }
}