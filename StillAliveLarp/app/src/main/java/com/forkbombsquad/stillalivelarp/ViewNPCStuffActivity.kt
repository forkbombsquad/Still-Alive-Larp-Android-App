package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.underline

class ViewNPCStuffActivity : NoStatusBarActivity() {

    private lateinit var NPCNameText: TextView

    private lateinit var infRating: KeyValueView
    private lateinit var bullets: KeyValueView
    private lateinit var skillsTree: NavArrowButtonBlack
    private lateinit var skillsList: NavArrowButtonBlack
    private lateinit var bio: NavArrowButtonBlack

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_npcstuff)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(NPCListActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        NPCNameText = findViewById(R.id.npcstuff_charName)
        infRating = findViewById(R.id.npcstuff_infection)
        bullets = findViewById(R.id.npcstuff_bullets)
        skillsTree = findViewById(R.id.npcstuff_skillsTreeNavArrow)
        skillsList = findViewById(R.id.npcstuff_skillsNavArrow)
        bio = findViewById(R.id.npcstuff_bioNavArrow)

        skillsTree.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, NPCPersonalNativeSkillTreeActivity::class.java)
            startActivity(intent)
        }
        skillsList.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, ViewSkillsActivity::class.java)
            startActivity(intent)
        }
        bio.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, ViewBioActivity::class.java)
            startActivity(intent)
        }

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(NPCNameText, "${character.fullName} (NPC${character.isAlive.ternary("", " - Dead")})")
        bullets.set(character.bullets.toString())
        infRating.set("${character.infection}%")

    }
}