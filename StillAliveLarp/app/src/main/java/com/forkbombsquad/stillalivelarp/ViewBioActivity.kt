package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope

import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen

class ViewBioActivity : NoStatusBarActivity() {

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
        text = findViewById(R.id.bio_text)

        edit.setOnClick {
            edit.setLoading(true)
            OldDataManager.shared.unrelaltedUpdateCallback = {
                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.CHARACTER), true) {
                    edit.setLoading(false)
                    OldDataManager.shared.charForSelectedPlayer = OldDataManager.shared.character
                    buildView()
                }
            }
            val intent = Intent(this, EditBioActivity::class.java)
            startActivity(intent)
        }

        buildView()
    }

    private fun buildView() {
        val player = OldDataManager.shared.selectedPlayer
        val char = OldDataManager.shared.charForSelectedPlayer

        edit.isGone = !(player?.id == OldDataManager.shared.player?.id && char?.characterTypeId == Constants.CharacterTypes.standard)

        title.text = "${char?.fullName ?: ""}'s\nBio"
        text.text = char?.bio ?: ""
    }
}