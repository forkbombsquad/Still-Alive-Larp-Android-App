package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable

class CharacterBioListActivity : NoStatusBarActivity() {

    private lateinit var loading: ProgressBar
    private lateinit var nobios: TextView
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_bio_list)
        setupView()
    }

    private fun setupView() {
        loading = findViewById(R.id.bioapproval_loading)
        nobios = findViewById(R.id.bioapproval_nobios)
        layout = findViewById(R.id.bioapproval_layout)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_CHARACTERS, OldDataManagerType.ALL_PLAYERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        val chars =
            (OldDataManager.shared.allCharacters ?: listOf()).filter { !it.approvedBio.toBoolean() && it.bio.trim().isNotEmpty() }
        if (OldDataManager.shared.loadingAllCharacters || OldDataManager.shared.loadingAllPlayers) {
            loading.isGone = false
            nobios.isGone = true
            layout.isGone = true
        } else if (chars.isEmpty()) {
            loading.isGone = true
            nobios.isGone = false
            layout.isGone = true
        } else {
            loading.isGone = true
            nobios.isGone = true
            layout.isGone = false
            for (char in chars) {
                val navarrow = NavArrowButtonBlackBuildable(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                navarrow.layoutParams = params

                navarrow.textView.text = char.fullName
                navarrow.setLoading(OldDataManager.shared.loadingEventPreregs)
                navarrow.setOnClick {
                    OldDataManager.shared.unrelaltedUpdateCallback = {
                        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_CHARACTERS), true) {
                            buildView()
                        }
                        buildView()
                    }
                    OldDataManager.shared.selectedPlayer = OldDataManager.shared.allPlayers?.firstOrNull { it.id == char.playerId }
                    OldDataManager.shared.selectedChar = char
                    val intent = Intent(this, ApproveBioActivity::class.java)
                    startActivity(intent)
                }
                layout.addView(navarrow)
            }
        }
    }

    override fun onBackPressed() {
        OldDataManager.shared.unrelaltedUpdateCallback()
        super.onBackPressed()
    }
}