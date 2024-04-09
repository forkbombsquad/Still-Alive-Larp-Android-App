package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

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

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_CHARACTERS, DataManagerType.ALL_PLAYERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        val chars =
            (DataManager.shared.allCharacters ?: listOf()).filter { !it.approvedBio.toBoolean() && it.bio.trim().isNotEmpty() }
        if (DataManager.shared.loadingAllCharacters || DataManager.shared.loadingAllPlayers) {
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
                navarrow.setLoading(DataManager.shared.loadingEventPreregs)
                navarrow.setOnClick {
                    DataManager.shared.unrelaltedUpdateCallback = {
                        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_CHARACTERS), true) {
                            buildView()
                        }
                        buildView()
                    }
                    DataManager.shared.selectedPlayer = DataManager.shared.allPlayers?.firstOrNull { it.id == char.playerId }
                    DataManager.shared.selectedChar = char
                    val intent = Intent(this, ApproveBioActivity::class.java)
                    startActivity(intent)
                }
                layout.addView(navarrow)
            }
        }
    }

    override fun onBackPressed() {
        DataManager.shared.unrelaltedUpdateCallback()
        super.onBackPressed()
    }
}