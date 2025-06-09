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
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment

import com.forkbombsquad.stillalivelarp.utils.GearCell

class ViewGearActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var layout: LinearLayout

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_gear)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.gear_title)
        layout = findViewById(R.id.gear_layout)

        character = DataManager.shared.getPassedData(listOf(MyAccountFragment::class, ViewPlayerActivity::class, ViewCharacterActivity::class, AdminPanelActivity::class), DataManagerPassedDataKey.SELECTED_CHARACTER)!!
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "${character.fullName}'s Gear")
        layout.removeAllViews()
        val gearList = character.getGearOrganized()
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