package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel

class GearCell(context: Context): LinearLayout(context) {

    val gearName: TextView
    val gearType: TextView
    val gearSubType: TextView
    val gearSecondarySubtype: TextView
    val gearDesc: TextView

    init {
        inflate(context, R.layout.gearcell, this)

        gearName = findViewById(R.id.gearcell_name)
        gearType = findViewById(R.id.gearcell_type)
        gearSubType = findViewById(R.id.gearcell_primarysubtype)
        gearSecondarySubtype = findViewById(R.id.gearcell_secondarysubtype)
        gearDesc = findViewById(R.id.gearcell_desc)
    }

    fun setup(gear: GearJsonModel) {
        gearName.text = gear.name
        gearType.text = gear.gearType
        gearSubType.text = gear.primarySubtype
        gearSecondarySubtype.text = gear.secondarySubtype
        gearDesc.text = gear.desc
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            callback()
        }
    }

}