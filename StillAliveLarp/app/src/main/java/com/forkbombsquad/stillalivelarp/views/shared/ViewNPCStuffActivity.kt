package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ternary

class ViewNPCStuffActivity : NoStatusBarActivity() {

    private lateinit var NPCNameText: TextView

    private lateinit var timesPlayed: KeyValueView
    private lateinit var infRating: KeyValueView
    private lateinit var bullets: KeyValueView
    private lateinit var mysteriousStranger: KeyValueView
    private lateinit var unshakableResolve: KeyValueView
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
        timesPlayed = findViewById(R.id.npcstuff_timesplayed)
        infRating = findViewById(R.id.npcstuff_infection)
        bullets = findViewById(R.id.npcstuff_bullets)
        mysteriousStranger = findViewById(R.id.npcstuff_ms)
        unshakableResolve = findViewById(R.id.npcstuff_ur)
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
            val intent = Intent(this, SkillsListActivity::class.java)
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
        mysteriousStranger.set("${character.mysteriousStrangerCount() - character.mysteriousStrangerUses} / ${character.mysteriousStrangerCount()}")
        unshakableResolve.set("${character.hasUnshakableResolve().ternary(1, 0) - character.unshakableResolveUses} / ${character.hasUnshakableResolve().ternary("1", "0")}")

        timesPlayed.set(DataManager.shared.events.flatMap { it.attendees }.count { it.npcId == character.id })

        mysteriousStranger.isGone = character.mysteriousStrangerCount() == 0
        unshakableResolve.isGone = !character.hasUnshakableResolve()
    }
}