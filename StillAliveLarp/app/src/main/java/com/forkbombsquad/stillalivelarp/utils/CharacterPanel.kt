package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel

class CharacterPanel(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val activeCharTitle: TextView
    val activeCharLayout: LinearLayout
    val viewStats: NavArrowButtonBlack
    val viewSkillsTree: NavArrowButtonBlack
    val viewSkillsList: NavArrowButtonBlack
    val viewBio: NavArrowButtonBlack
    val viewGear: NavArrowButtonBlack
    val viewAwards: NavArrowButtonBlack

    val otherCharsTitle: TextView
    val otherCharsLayout: LinearLayout
    val viewInactiveChars: NavArrowButtonBlack
    val viewPlannedChars: NavArrowButtonBlue

    init {
        inflate(context, R.layout.character_panel, this)

        activeCharTitle = findViewById(R.id.charpanel_activeCharTitle)
        activeCharLayout = findViewById(R.id.charpanel_activeCharContentContainer)
        viewStats = findViewById(R.id.charpanel_viewStats)
        viewSkillsTree = findViewById(R.id.charpanel_viewSkillsTree)
        viewSkillsList = findViewById(R.id.charpanel_viewSkillsList)
        viewBio = findViewById(R.id.charpanel_viewBio)
        viewGear = findViewById(R.id.charpanel_viewGear)
        viewAwards = findViewById(R.id.charpanel_viewAwards)

        otherCharsTitle = findViewById(R.id.charpanel_otherCharacters)
        otherCharsLayout = findViewById(R.id.charpanel_otherCharsContentContainer)
        viewInactiveChars = findViewById(R.id.charpanel_viewInactiveCharacters)
        viewPlannedChars = findViewById(R.id.charpanel_viewPlannedCharacters)
    }

    fun setValuesAndHideViews(char: FullCharacterModel?, player: FullPlayerModel) {
        char.ifLet({ character ->
            var acText = ""
            when (character.characterType()) {
                CharacterType.STANDARD -> {
                    acText = if (character.isAlive) {
                        "(Active Character)"
                    } else {
                        "(Inactive Character)"
                    }
                }
                CharacterType.NPC -> {
                    acText = "(NPC${character.isAlive.ternary("", " - Dead")})"
                }
                CharacterType.PLANNER -> {
                    acText = "(Planned Character)"
                }
                CharacterType.HIDDEN -> {
                    acText = "(Hidden Character)"
                }
            }
            activeCharTitle.text = "${character.fullName} $acText"

            val isOwnedByPlayer = player.id == DataManager.shared.getCurrentPlayer()?.id
            val charIsStandard = character.characterType() == CharacterType.STANDARD

            if (!charIsStandard || !character.isAlive) {
                otherCharsTitle.isGone = true
                otherCharsLayout.isGone = true
            }

            if (charIsStandard) {
                viewBio.isGone = !character.approvedBio
                viewInactiveChars.isGone = player.getInactiveCharacters().isEmpty() == true
                viewPlannedChars.isGone = player.getPlannedCharacters().isEmpty() == true
                if (isOwnedByPlayer) {
                    viewBio.isGone = false
                    viewPlannedChars.isGone = false
                }

                if (viewInactiveChars.isGone && viewPlannedChars.isGone) {
                    otherCharsTitle.isGone = true
                    otherCharsLayout.isGone = true
                } else {
                    otherCharsTitle.isGone = false
                    otherCharsLayout.isGone = false
                }
            } else {
                viewBio.isGone = character.characterType() == CharacterType.NPC
            }
        }, {
            activeCharTitle.isGone = true
            activeCharLayout.isGone = true
            viewInactiveChars.isGone = player.getInactiveCharacters().isEmpty() == true
            viewPlannedChars.isGone = player.getPlannedCharacters().isEmpty() == true
        })
    }

    fun setOnClicks(
        viewStatsCallback: () -> Unit,
        viewSkillsTreeCallback: () -> Unit,
        viewSkillsListCallback: () -> Unit,
        viewBioCallback: () -> Unit,
        viewGearCallback: () -> Unit,
        viewAwardsCallback: () -> Unit,
        viewInactiveCharsCallback: () -> Unit,
        viewPlannedCharsCallback: () -> Unit
    ) {
        viewStats.setOnClick(viewStatsCallback)
        viewSkillsTree.setOnClick(viewSkillsTreeCallback)
        viewSkillsList.setOnClick(viewSkillsListCallback)
        viewBio.setOnClick(viewBioCallback)
        viewGear.setOnClick(viewGearCallback)
        viewAwards.setOnClick(viewAwardsCallback)
        viewInactiveChars.setOnClick(viewInactiveCharsCallback)
        viewPlannedChars.setOnClick(viewPlannedCharsCallback)
    }

}