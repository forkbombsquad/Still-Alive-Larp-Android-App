package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager

class ActivityTemplate : NoStatusBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
        setupView()
    }

    private fun setupView() {

        OldDataManager.shared.load(lifecycleScope, listOf(), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {

    }
}