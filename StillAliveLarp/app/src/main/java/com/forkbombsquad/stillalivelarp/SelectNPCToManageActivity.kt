package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope

import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class SelectNPCToManageActivity : NoStatusBarActivity() {

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_npc_to_manage)
        setupView()
    }

    private fun setupView() {
        layout = findViewById(R.id.npcs_layout)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_NPC_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()

        OldDataManager.shared.unrelaltedUpdateCallback = {
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_NPC_CHARACTERS), true) {
                buildView()
            }
            buildView()
        }
        OldDataManager.shared.allNPCCharacters.ifLet { chars ->
            val living = chars.filter { it.isAlive.toBoolean() }
            living.alphabetized().forEachIndexed { index, char ->
                val arrow = NavArrowButtonBlackBuildable(this)
                arrow.textView.text = char.fullName
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    arrow.setLoading(true)
                    CharacterManager.shared.fetchFullCharacter(lifecycleScope, char.id) {
                        OldDataManager.shared.selectedNPCCharacter = it
                        arrow.setLoading(false)
                        val intent = Intent(this@SelectNPCToManageActivity, ManageNPCActivity::class.java)
                        startActivity(intent)
                    }
                }
                layout.addView(arrow)
            }
            chars.filter { !it.isAlive.toBoolean() }.alphabetized().forEachIndexed { index, char ->
                val arrow = NavArrowButtonRedBuildable(this)
                arrow.textView.text = char.fullName + " (Dead)"
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    arrow.setLoading(true)
                    CharacterManager.shared.fetchFullCharacter(lifecycleScope, char.id) {
                        OldDataManager.shared.selectedNPCCharacter = it
                        arrow.setLoading(false)
                        val intent = Intent(this@SelectNPCToManageActivity, ManageNPCActivity::class.java)
                        startActivity(intent)
                    }
                }
                layout.addView(arrow)
            }
        }
    }
}