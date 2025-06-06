package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import kotlin.math.max

class SkillCell(context: Context): LinearLayout(context) {

    val title: TextView
    val type: TextView
    val infoLayout: LinearLayout
    val xp: TextView
    val pp: TextView
    val inf: TextView
    val prereqLayout: LinearLayout
    val prereqs: TextView
    val desc: TextView

    // Purchasing
    val purchaseLayout: LinearLayout
    val purchaseCost: TextView
    val purchasePpCost: TextView
    val purchaseInfThreshold: TextView
    val purchaseButton: LoadingButton

    init {
        inflate(context, R.layout.skillcell, this)

        title = findViewById(R.id.skillcell_title)
        type = findViewById(R.id.skillcell_type)
        infoLayout = findViewById(R.id.skillcell_infocost)
        xp = findViewById(R.id.skillcell_xp)
        pp = findViewById(R.id.skillcell_pp)
        inf = findViewById(R.id.skillcell_inf)
        prereqLayout = findViewById(R.id.skillcell_prereqLayout)
        prereqs = findViewById(R.id.skillcell_prereqs)
        desc = findViewById(R.id.skillcell_desc)

        purchaseLayout = findViewById(R.id.skillcell_purchaseCostLayout)
        purchaseCost = findViewById(R.id.skillcell_purchaseCost)
        purchasePpCost = findViewById(R.id.skillcell_purchasePpCost)
        purchaseInfThreshold = findViewById(R.id.skillcell_purchaseInfThreshold)
        purchaseButton = findViewById(R.id.skillcell_purchaseButton)
    }

    fun setup(skill: FullCharacterModifiedSkillModel) {
        title.text = skill.name
        type.text = skill.getTypeText()

        infoLayout.isGone = false
        purchaseButton.isGone = true
        purchaseLayout.isGone = true

        xp.text = skill.getXpCostText()
        pp.text = "and ${skill.getPrestigeCostText()}"
        inf.text = skill.getInfCostText()

        pp.isGone = skill.prestigeCost() == 0
        inf.isGone = skill.modInfectionCost() == 0

        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> type.setTextColor(context.getColor(R.color.bright_red))
            Constants.SkillTypes.profession -> type.setTextColor(context.getColor(R.color.green))
            Constants.SkillTypes.talent -> type.setTextColor(context.getColor(R.color.blue))
        }

        prereqLayout.isGone = skill.prereqs().isEmpty()
        prereqs.text = skill.getPrereqNames()

        desc.text = skill.description

    }

    fun setupForXpReduction(skill: FullCharacterModifiedSkillModel, buttonCallback: (skill: FullCharacterModifiedSkillModel) -> Unit) {
        title.text = skill.name
        type.text = skill.getTypeText()

        infoLayout.isGone = false
        purchaseLayout.isGone = true

        xp.text = skill.getXpCostText()
        pp.text = skill.getPrestigeCostText()
        inf.text = skill.getInfCostText()

        pp.isGone = skill.prestigeCost() == 0
        inf.isGone = skill.modInfectionCost() == 0

        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> type.setTextColor(context.getColor(R.color.bright_red))
            Constants.SkillTypes.profession -> type.setTextColor(context.getColor(R.color.green))
            Constants.SkillTypes.talent -> type.setTextColor(context.getColor(R.color.blue))
        }

        prereqLayout.isGone = !skill.hasPrereqs()
        prereqs.text = skill.getPrereqNames()

        desc.text = skill.description

        purchaseButton.set("Give Xp Reduction")
        purchaseButton.setOnClick {
            if (skill.modXpCost() == 1) {
                AlertUtils.displayError(context, "You can't reduce the cost of this skill below 1xp!")
            } else {
                buttonCallback(skill)
            }
        }
    }

    fun setupForPurchase(skill: FullCharacterModifiedSkillModel, player: FullPlayerModel, forPlannedCharacterOrNPC: Boolean = false, buttonCallback: (skill: FullCharacterModifiedSkillModel) -> Unit) {
        title.text = skill.name
        type.text = skill.getTypeText()

        purchaseLayout.isGone = false
        purchaseButton.isGone = false
        infoLayout.isGone = true

        purchaseButton.setOnClick {
            buttonCallback(skill)
        }

        val xpText = skill.getXpCostText(player.freeTier1Skills > 0 && !forPlannedCharacterOrNPC)
        purchaseCost.text = xpText
        if (xpText.containsIgnoreCase("free")) {
            purchaseCost.setTextColor(context.getColor(R.color.green))
        } else if (skill.hasModCost()) {
            purchaseCost.setTextColor(getColorForXp(skill))
        } else {
            purchaseCost.setTextColor(context.getColor(R.color.black))
        }

        purchasePpCost.text = skill.getPrestigeCostText()
        purchasePpCost.setTextColor(context.getColor(R.color.blue))
        purchasePpCost.isGone = !skill.usesPrestige()

        purchaseInfThreshold.text = "${forPlannedCharacterOrNPC.ternary("Will Require: ", "Your Infection Rating meets the required")} ${skill.getInfCostText()}"
        if (skill.hasModInfCost()) {
            purchaseInfThreshold.setTextColor(getColorForInf(skill))
        } else {
            purchaseInfThreshold.setTextColor(context.getColor(R.color.black))
        }
        purchaseInfThreshold.isGone = !skill.usesInfection()

        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> type.setTextColor(context.getColor(R.color.bright_red))
            Constants.SkillTypes.profession -> type.setTextColor(context.getColor(R.color.green))
            Constants.SkillTypes.talent -> type.setTextColor(context.getColor(R.color.blue))
        }

        prereqs.text = skill.getPrereqNames()
        prereqLayout.isGone = !skill.hasPrereqs()

        desc.text = skill.description
    }

    private fun getColorForXp(skill: FullCharacterModifiedSkillModel): Int {
        return if (skill.hasModCost()) {
            if (skill.modXpCost() > skill.baseXpCost()) {
                context.getColor(R.color.bright_red)
            } else {
                context.getColor(R.color.green)
            }
        } else {
            context.getColor(R.color.black)
        }
    }

    private fun getColorForInf(skill: FullCharacterModifiedSkillModel): Int {
        return if (skill.hasModInfCost()) {
            if (skill.modInfectionCost() > skill.baseInfectionCost()) {
                context.getColor(R.color.bright_red)
            } else {
                context.getColor(R.color.green)
            }
        } else {
            context.getColor(R.color.black)
        }
    }

}