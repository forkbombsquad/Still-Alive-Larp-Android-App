package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R

import com.forkbombsquad.stillalivelarp.services.models.EventPreregModel
import com.forkbombsquad.stillalivelarp.services.models.EventRegType

class PreregCell(context: Context): LinearLayout(context) {

    val playerName: KeyValueView
    val charName: KeyValueView
    val regType: KeyValueView

    init {
        inflate(context, R.layout.prereg_cell, this)

        playerName = findViewById(R.id.preregcell_playerName)
        charName = findViewById(R.id.preregcell_charName)
        regType = findViewById(R.id.preregcell_regType)
    }

    fun set(eventPrereg: EventPreregModel) {
        val players = OldDataManager.shared.allPlayers ?: listOf()
        val chars = OldDataManager.shared.allCharacters ?: listOf()

        playerName.set(players.firstOrNull { it.id == eventPrereg.playerId }?.fullName ?: "")
        charName.set(chars.firstOrNull { it.id == eventPrereg.getCharId() }?.fullName ?: "NPC")
        charName.isGone = eventPrereg.eventRegType() == EventRegType.NOT_PREREGED
        regType.set(eventPrereg.eventRegType().getAttendingText())
    }

}