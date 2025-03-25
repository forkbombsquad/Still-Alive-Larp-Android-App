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
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class NPCListActivity : NoStatusBarActivity() {

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_npclist)
        setupView()
    }

    private fun setupView() {
        layout = findViewById(R.id.npcs_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_NPC_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()

        DataManager.shared.allNPCCharacters.ifLet { chars ->
            chars.filter { it.isAlive.toBoolean() }.alphabetized().forEachIndexed { index, char ->
                val arrow = NavArrowButtonBlackBuildable(this)
                arrow.textView.text = char.fullName
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    DataManager.shared.selectedChar = char
                    // TODO view character stats and skills
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
                    DataManager.shared.selectedChar = char
                    // TODO view character stats and character skills
                }
                layout.addView(arrow)
            }
        }
    }
}