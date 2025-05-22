package com.forkbombsquad.stillalivelarp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.utils.GearCell

class ViewGearActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var progressbar: ProgressBar
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_gear)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.gear_title)
        progressbar = findViewById(R.id.gear_progressbar)
        layout = findViewById(R.id.gear_layout)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.SELECTED_CHARACTER_GEAR), true) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "${OldDataManager.shared.selectedChar?.fullName ?: ""}'s Gear"
        if (OldDataManager.shared.loadingSelectedCharacterGear) {
            progressbar.isGone = false
            layout.isGone = true
        } else {
            progressbar.isGone = true
            layout.isGone = false

            layout.removeAllViews()
            val gearList = OldDataManager.shared.getGearOrganzied()
            gearList.forEach { (key, list) ->
                val textView = TextView(this)
                val tvParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                tvParams.setMargins(0, 8, 0, 8)
                textView.layoutParams = tvParams
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                textView.setTypeface(null, Typeface.BOLD)
                textView.setTextColor(Color.BLACK)
                textView.text = key
                layout.addView(textView)

                list.forEach { g ->
                    val gearCell = GearCell(this)
                    gearCell.setup(g)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 8, 0, 8)
                    gearCell.layoutParams = params
                    layout.addView(gearCell)
                }
            }
        }
    }
}