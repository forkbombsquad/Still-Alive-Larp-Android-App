package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.underline

class ViewNPCStuffActivity : NoStatusBarActivity() {

    private lateinit var NPCNameText: TextView

    private lateinit var infRating: KeyValueView
    private lateinit var bullets: KeyValueView
    private lateinit var skills: NavArrowButtonBlack
    private lateinit var bio: NavArrowButtonBlack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_npcstuff)
        setupView()
    }

    private fun setupView() {
        OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
        OldDataManager.shared.charForSelectedPlayer = OldDataManager.shared.selectedNPCCharacter

        NPCNameText = findViewById(R.id.npcstuff_charName)
        infRating = findViewById(R.id.npcstuff_infection)
        bullets = findViewById(R.id.npcstuff_bullets)
        skills = findViewById(R.id.npcstuff_skillsNavArrow)
        bio = findViewById(R.id.npcstuff_bioNavArrow)

        skills.setOnClick {
            val intent = Intent(this, OfflineViewSkillsActivity::class.java)
            startActivity(intent)
        }
        bio.setOnClick {
            val intent = Intent(this, ViewBioActivity::class.java)
            startActivity(intent)
        }

        buildView()
    }

    private fun buildView() {
        val character = OldDataManager.shared.charForSelectedPlayer
        character.ifLet({
            NPCNameText.text = it.fullName.underline()
            bullets.set(it.bullets)
            infRating.set(it.infection + "%")
        }, {
            NPCNameText.text = "NPC"
        })

    }
}