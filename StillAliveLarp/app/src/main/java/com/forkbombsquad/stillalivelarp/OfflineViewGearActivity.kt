package com.forkbombsquad.stillalivelarp

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.utils.GearCell
import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.divider.MaterialDivider

class OfflineViewGearActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_view_gear)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.offlinegear_title)
        layout = findViewById(R.id.offlinegear_layout)

        buildView()
    }

    private fun buildView() {
        title.text = "Offline\n${DataManager.shared.selectedChar?.fullName ?: ""} Gear"
        layout.isGone = false

        layout.removeAllViews()

        val gearList = DataManager.shared.getGearOrganzied()
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