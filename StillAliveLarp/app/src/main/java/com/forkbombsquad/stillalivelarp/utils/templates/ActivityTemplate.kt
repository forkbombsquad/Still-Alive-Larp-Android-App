package com.forkbombsquad.stillalivelarp.utils.templates

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager


class ActivityTemplate : NoStatusBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
        setupView()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
        })
        buildView()
    }

    private fun setupView() {

        buildView()
    }

    private fun buildView() {

    }
}