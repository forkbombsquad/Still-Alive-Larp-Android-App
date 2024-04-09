package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized

class SelectCharacterForClassXpReductionActivity : NoStatusBarActivity() {

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_character_for_class_xp_reduction)
        setupView()
    }

    private fun setupView() {

        layout = findViewById(R.id.selectcharforclassxpred_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        val chars = (DataManager.shared.allCharacters ?: listOf())

        for (char in chars.alphabetized()) {
            val navarrow = NavArrowButtonBlackBuildable(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 16, 0, 16)
            navarrow.layoutParams = params

            navarrow.textView.text = char.fullName
            navarrow.setLoading(DataManager.shared.loadingEventPreregs)
            navarrow.setOnClick {

                DataManager.shared.activityToClose = this
                DataManager.shared.selectedPlayer = DataManager.shared.allPlayers?.firstOrNull { it.id == char.playerId }
                DataManager.shared.selectedChar = char

                val intent = Intent(this, SelectSkillForClassXpReductionActivity::class.java)
                startActivity(intent)

            }
            layout.addView(navarrow)
        }
    }
}