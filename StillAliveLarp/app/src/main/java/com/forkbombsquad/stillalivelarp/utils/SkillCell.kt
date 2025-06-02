package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
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

    fun setup(skill: FullSkillModel) {
        title.text = skill.name
        type.text = skill.getTypeText()

        infoLayout.isGone = false

        xp.text = "${skill.xpCost}xp"
        pp.text = "${skill.prestigeCost}pp"
        inf.text = "${skill.minInfection}% Inf Threshold"

        pp.isGone = skill.prestigeCost == 0
        inf.isGone = skill.minInfection == 0

        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> type.setTextColor(context.getColor(R.color.bright_red))
            Constants.SkillTypes.profession -> type.setTextColor(context.getColor(R.color.green))
            Constants.SkillTypes.talent -> type.setTextColor(context.getColor(R.color.blue))
        }

        prereqLayout.isGone = skill.prereqs.isEmpty()
        prereqs.text = skill.getPrereqNames()

        desc.text = skill.description

        purchaseButton.isGone = true
        purchaseLayout.isGone = true
    }

    fun setupForXpReduction(skill: FullCharacterModifiedSkillModel, buttonCallback: (skill: FullCharacterModifiedSkillModel) -> Unit) {
        title.text = skill.name
        type.text = skill.getTypeText()

        infoLayout.isGone = false

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

        purchaseLayout.isGone = true
    }

    fun setupForPurchase(skill: FullCharacterModifiedSkillModel, player: PlayerModel, buttonCallback: (skill: FullCharacterModifiedSkillModel) -> Unit) {
        title.text = skill.name
        type.text = skill.getTypeText()

        purchaseLayout.isGone = false
        purchaseButton.isGone = false

        purchaseButton.setOnClick {
            buttonCallback(skill)
        }

        if (skill.canUseFreeSkill() && player.freeTier1Skills.toInt() > 0) {
            purchaseCost.text = "Cost: 1 Free Tier-1 Skill"
            purchaseCost.setTextColor(context.getColor(R.color.green))
        } else if (skill.hasModCost()) {
            purchaseCost.text = skill.getXpCostText()
            purchaseCost.setTextColor(getColorForXp(skill))
        } else {
            purchaseCost.text = skill.getXpCostText()
            purchaseCost.setTextColor(context.getColor(R.color.black))
        }

        purchasePpCost.isGone = !skill.usesPrestige()

        if (skill.usesPrestige()) {
            purchasePpCost.text = "and ${skill.prestigeCost()}pp"
            purchasePpCost.setTextColor(context.getColor(R.color.blue))
        }

        purchaseInfThreshold.isGone = !skill.usesInfection()
        if (skill.usesInfection()) {
            if (skill.hasModInfCost()) {
                purchaseInfThreshold.text = "Your infection rating meets the required ${skill.getInfCostText()}"
                purchaseInfThreshold.setTextColor(getColorForInf(skill))
            } else {
                purchaseInfThreshold.text = "Your infection rating meets the required ${skill.getInfCostText()}"
                purchaseInfThreshold.setTextColor(context.getColor(R.color.black))
            }
        }

        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> type.setTextColor(context.getColor(R.color.bright_red))
            Constants.SkillTypes.profession -> type.setTextColor(context.getColor(R.color.green))
            Constants.SkillTypes.talent -> type.setTextColor(context.getColor(R.color.blue))
        }

        prereqLayout.isGone = !skill.hasPrereqs()
        prereqs.text = skill.getPrereqNames()
        desc.text = skill.description

        infoLayout.isGone = true
    }

    fun setupForPlannedPurchase(skill: FullCharacterModifiedSkillModel, buttonCallback: (skill: FullCharacterModifiedSkillModel) -> Unit) {
        title.text = skill.name
        type.text = skill.getTypeText()

        purchaseLayout.isGone = false
        purchaseButton.isGone = false

        purchaseButton.setOnClick {
            buttonCallback(skill)
        }

        if (skill.canUseFreeSkill()) {
            purchaseCost.text = "Cost: 1 Free Tier-1 Skill or ${skill.getXpCostText()}"
            purchaseCost.setTextColor(context.getColor(R.color.green))
        } else if (skill.hasModCost()) {
            purchaseCost.text = skill.getXpCostText()
            purchaseCost.setTextColor(getColorForXp(skill))
        } else {
            purchaseCost.text = skill.getXpCostText()
            purchaseCost.setTextColor(context.getColor(R.color.black))
        }

        purchasePpCost.isGone = !skill.usesPrestige()

        if (skill.usesPrestige()) {
            purchasePpCost.text = "and ${skill.prestigeCost()}pp"
            purchasePpCost.setTextColor(context.getColor(R.color.blue))
        }

        purchaseInfThreshold.isGone = !skill.usesInfection()
        if (skill.usesInfection()) {
            if (skill.hasModInfCost()) {
                purchaseInfThreshold.text = "Requires ${skill.getInfCostText()}"
                purchaseInfThreshold.setTextColor(getColorForInf(skill))
            } else {
                purchaseInfThreshold.text = "Requires ${skill.getInfCostText()}"
                purchaseInfThreshold.setTextColor(context.getColor(R.color.black))
            }
        }

        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> type.setTextColor(context.getColor(R.color.bright_red))
            Constants.SkillTypes.profession -> type.setTextColor(context.getColor(R.color.green))
            Constants.SkillTypes.talent -> type.setTextColor(context.getColor(R.color.blue))
        }

        prereqLayout.isGone = !skill.hasPrereqs()
        prereqs.text = skill.getPrereqNames()
        desc.text = skill.description

        infoLayout.isGone = true
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