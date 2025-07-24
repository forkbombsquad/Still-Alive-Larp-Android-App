package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.CampFortification
import com.forkbombsquad.stillalivelarp.services.models.CampFortifications
import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.services.models.Fortification
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import kotlin.math.max

class FortificationCell(context: Context): LinearLayout(context) {

    val title: TextView
    val health: KeyValueView

    init {
        inflate(context, R.layout.fortificationcell, this)

        title = findViewById(R.id.fc_title)
        health = findViewById(R.id.fc_health)
    }

    fun setup(fortification: Fortification) {
        title.text = "${fortification.fortificationType.text.capitalizeOnlyFirstLetterOfEachWord()} Fortification"
        health.set("${fortification.health} / ${fortification.fortificationType.getMaxHealth()}")
    }

    fun setupEmpty() {
        title.text = "Empty Fortification Slot"
        health.set("0 / 0")
    }

}