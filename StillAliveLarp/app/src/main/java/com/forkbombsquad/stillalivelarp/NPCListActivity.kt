package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.CharacterManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class NPCListActivity : NoStatusBarActivity() {

    private lateinit var livingNPCs: KeyValueView
    private lateinit var rewardReduction: KeyValueView
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_npclist)
        setupView()
    }

    private fun setupView() {
        livingNPCs = findViewById(R.id.npcs_total)
        rewardReduction = findViewById(R.id.npcs_lootRatio)
        layout = findViewById(R.id.npcs_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_NPC_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()

        DataManager.shared.allNPCCharacters.ifLet { chars ->
            val living = chars.filter { it.isAlive.toBoolean() }

            livingNPCs.set("${living.count()} / 10")
            rewardReduction.set("${(10 - living.count()) * 10}%")

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
                        DataManager.shared.selectedNPCCharacter = it
                        arrow.setLoading(false)
                        val intent = Intent(this@NPCListActivity, ViewNPCStuffActivity::class.java)
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
                        DataManager.shared.selectedNPCCharacter = it
                        arrow.setLoading(false)
                        val intent = Intent(this@NPCListActivity, ViewNPCStuffActivity::class.java)
                        startActivity(intent)
                    }
                }
                layout.addView(arrow)
            }
        }
    }
}