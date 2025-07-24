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
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import kotlin.math.max

class FortificationRingCell(context: Context): LinearLayout(context) {

    val title: TextView
    val progressBar: ProgressBar
    val slots: KeyValueView
    val fortLayout: LinearLayout

    private var onClick = {}

    init {
        inflate(context, R.layout.fortificationringcell, this)

        title = findViewById(R.id.frc_title)
        progressBar = findViewById(R.id.frc_progressbar)
        slots = findViewById(R.id.frc_slots)
        fortLayout = findViewById(R.id.frc_fortlayout)

        this.setOnClickListener {
            if (progressBar.isGone) {
                onClick()
            }
        }
    }

    fun setup(context: Context, campFortification: CampFortification) {
        title.text = "Ring ${campFortification.ring}"
        slots.set("${campFortification.fortifications.count()} / ${campFortification.ring}")
        fortLayout.removeAllViews()
        var count = 0
        campFortification.fortifications.forEach {
            val cell = FortificationCell(context)
            cell.setup(it)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, 16, 8, 16)
            fortLayout.addView(cell)
            count += 1
        }
        while (count < campFortification.ring) {
            val cell = FortificationCell(context)
            cell.setupEmpty()
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, 16, 8, 16)
            fortLayout.addView(cell)
            count += 1
        }
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        slots.isGone = loading
        fortLayout.isGone = loading
    }

    fun setOnClick(onClick: () -> Unit) {
        this.onClick = onClick
    }

}