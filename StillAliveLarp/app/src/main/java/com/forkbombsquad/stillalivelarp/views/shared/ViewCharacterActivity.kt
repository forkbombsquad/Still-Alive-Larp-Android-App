package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.views.account.CharacterPlannerActivity

class ViewCharacterActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var playerName: KeyValueView
    private lateinit var viewStats: NavArrowButtonBlack
    private lateinit var viewSkillsTree: NavArrowButtonBlack
    private lateinit var viewSkillsList: NavArrowButtonBlack
    private lateinit var viewBio: NavArrowButtonBlack
    private lateinit var viewGear: NavArrowButtonBlack
    private lateinit var viewXpReductions: NavArrowButtonBlack
    private lateinit var viewAwards: NavArrowButtonBlack

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_character)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(listOf(CharactersListActivity::class, CharacterPlannerActivity::class), DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.viewchar_title)
        playerName = findViewById(R.id.viewchar_playerName)
        viewStats = findViewById(R.id.charview_viewStats)
        viewSkillsTree = findViewById(R.id.charview_viewSkillsTree)
        viewSkillsList = findViewById(R.id.charview_viewSkillsList)
        viewBio = findViewById(R.id.charview_viewBio)
        viewGear = findViewById(R.id.charview_viewGear)
        viewXpReductions = findViewById(R.id.charview_viewXpReductions)
        viewAwards = findViewById(R.id.charview_viewAwards)

        viewStats.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            DataManager.shared.setUpdateCallback(this::class) {
                buildView()
            }
            val intent = Intent(this, ViewCharacterStatsActivity::class.java)
            startActivity(intent)
        }

        viewSkillsTree.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            when (character.characterType()) {
                CharacterType.STANDARD, CharacterType.HIDDEN -> {
                    if (DataManager.shared.playerIsCurrentPlayer(character.id) && character.isAlive) {
                        val intent = Intent(this, PersonalNativeSkillTreeActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, OtherCharacterPersonalNativeSkillTreeActivity::class.java)
                        startActivity(intent)
                    }
                }
                CharacterType.NPC -> {
                    val intent = Intent(this, NPCPersonalNativeSkillTreeActivity::class.java)
                    startActivity(intent)
                }
                CharacterType.PLANNER -> {
                    val intent = if (DataManager.shared.playerIsCurrentPlayer(character.playerId)) {
                        Intent(this, PlannedCharacterPersonalNativeSkillTreeActivity::class.java)
                    } else {
                        Intent(this, OtherCharacterPersonalNativeSkillTreeActivity::class.java)
                    }
                    startActivity(intent)
                }
            }
        }

        viewSkillsList.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            if (character.characterType() == CharacterType.PLANNER && DataManager.shared.playerIsCurrentPlayer(character.playerId)) {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.ACTION, SkillsListActivity.SkillsListActivityActions.ALLOW_DELETE)
            }
            val intent = Intent(this, SkillsListActivity::class.java)
            startActivity(intent)
        }

        viewBio.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, ViewBioActivity::class.java)
            startActivity(intent)
        }

        viewGear.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, ViewGearActivity::class.java)
            startActivity(intent)
        }

        viewXpReductions.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, XpReductionsListActivity::class.java)
            startActivity(intent)
        }

        viewAwards.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.AWARDS_LIST, character.getAwardsSorted())
            val intent = Intent(this, ViewAwardsActivity::class.java)
            startActivity(intent)
        }
        
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "${character.fullName}\n(${character.getPostText()})")

        when (character.characterType()) {
            CharacterType.STANDARD -> {
                playerName.isGone = false
                var showBio = character.approvedBio
                if (!showBio) {
                    showBio = DataManager.shared.playerIsCurrentPlayer(character.id)
                }
                viewBio.isGone = !showBio
                viewGear.isGone = false
                viewAwards.isGone = false
                viewXpReductions.isGone = false
            }
            CharacterType.PLANNER, CharacterType.NPC, CharacterType.HIDDEN -> {
                playerName.isGone = true
                viewBio.isGone = character.characterType() == CharacterType.PLANNER
                viewGear.isGone = true
                viewAwards.isGone = true
                viewXpReductions.isGone = true
            }
        }

        playerName.set(DataManager.shared.getPlayerForCharacter(character).fullName)
    }
}