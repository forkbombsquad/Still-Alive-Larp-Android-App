package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen

class OfflineViewBioActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var edit: NavArrowButtonGreen
    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bio)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.bio_title)
        edit = findViewById(R.id.bio_edit)
        edit.isGone = true
        text = findViewById(R.id.bio_text)

        buildView()
    }

    private fun buildView() {
        val player = DataManager.shared.selectedPlayer
        val char = DataManager.shared.charForSelectedPlayer

        title.text = "${char?.fullName ?: ""}'s\nBio (Offline)"
        text.text = char?.bio ?: ""
    }
}