package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class NPCListActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var livingNPCs: KeyValueView
    private lateinit var rewardReduction: KeyValueView
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_npclist)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.npcs_title)
        livingNPCs = findViewById(R.id.npcs_total)
        rewardReduction = findViewById(R.id.npcs_lootRatio)
        layout = findViewById(R.id.npcs_layout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "All NPCs")
        layout.removeAllViews()

        val chars = DataManager.shared.getAllCharacters(CharacterType.NPC)
        val living = chars.filter { it.isAlive }

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
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, char)
                val intent = Intent(this@NPCListActivity, ViewNPCStuffActivity::class.java)
                startActivity(intent)
            }
            layout.addView(arrow)
        }
        chars.filter { !it.isAlive }.alphabetized().forEachIndexed { index, char ->
            val arrow = NavArrowButtonRedBuildable(this)
            arrow.textView.text = "${char.fullName} (Dead)"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, char)
                val intent = Intent(this@NPCListActivity, ViewNPCStuffActivity::class.java)
                startActivity(intent)
            }
            layout.addView(arrow)
        }
    }
}