package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.CommunityFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlin.reflect.KClass

class NPCListActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var livingNPCs: KeyValueView
    private lateinit var rewardReduction: KeyValueView
    private lateinit var layout: LinearLayout

    private lateinit var destClass: KClass<*>
    private lateinit var characters: List<FullCharacterModel>
    private lateinit var viewTitle: String

    private val sourceClasses: List<KClass<*>> = listOf(CommunityFragment::class, AdminPanelActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_npclist)
        setupView()
    }

    private fun setupView() {
        destClass = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.DESTINATION_CLASS)!!
        characters = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.CHARACTER_LIST)!!
        viewTitle = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.VIEW_TITLE)!!

        title = findViewById(R.id.npcs_title)
        livingNPCs = findViewById(R.id.npcs_total)
        rewardReduction = findViewById(R.id.npcs_lootRatio)
        layout = findViewById(R.id.npcs_layout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, viewTitle)
        layout.removeAllViews()

        val living = characters.filter { it.isAlive }

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
                DataManager.shared.addActivityToClose(this)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, char)
                val intent = Intent(this, destClass.java)
                startActivity(intent)
            }
            layout.addView(arrow)
        }
        characters.filter { !it.isAlive }.alphabetized().forEachIndexed { index, char ->
            val arrow = NavArrowButtonRedBuildable(this)
            arrow.textView.text = "${char.fullName} (Dead)"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                DataManager.shared.addActivityToClose(this)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, char)
                val intent = Intent(this, destClass.java)
                startActivity(intent)
            }
            layout.addView(arrow)
        }
    }
}