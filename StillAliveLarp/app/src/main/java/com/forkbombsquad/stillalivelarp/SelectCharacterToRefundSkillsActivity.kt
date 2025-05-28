package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope

import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class SelectCharacterToRefundSkillsActivity : NoStatusBarActivity() {

    private lateinit var layout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_character_to_refund_skills)
        setupView()
    }

    private fun setupView() {
        layout = findViewById(R.id.selectchartorefund_layout)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        OldDataManager.shared.allCharacters.ifLet { chars ->
            chars.alphabetized().forEachIndexed { index, char ->
                val arrow = NavArrowButtonBlackBuildable(this)
                arrow.textView.text = char.fullName
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    OldDataManager.shared.selectedChar = char
                    val intent = Intent(this, RefundSkillsActivity::class.java)
                    startActivity(intent)
                }
                layout.addView(arrow)
            }
        }
    }
}