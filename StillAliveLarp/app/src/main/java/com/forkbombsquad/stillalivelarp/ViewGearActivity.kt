package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.primaryWeapon
import com.forkbombsquad.stillalivelarp.services.models.removingPrimaryWeapon
import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.google.android.material.divider.MaterialDivider

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

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SELECTED_CHARACTER_GEAR), true) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "${DataManager.shared.selectedChar?.fullName ?: ""}'s Gear"
        if (DataManager.shared.loadingSelectedCharacterGear) {
            progressbar.isGone = false
            layout.isGone = true
        } else {
            progressbar.isGone = true
            layout.isGone = false

            layout.removeAllViews()

            DataManager.shared.selectedCharacterGear.ifLet { gear ->
                gear.primaryWeapon().ifLet { primaryWeaopn ->
                    val container = LinearLayout(this)
                    container.setPadding(0, 8, 0, 0)
                    container.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    container.orientation = LinearLayout.VERTICAL

                    val kvView = KeyValueViewBuildable(this)
                    kvView.set("Primary Weapon", primaryWeaopn.name, showDiv = false)

                    val desc = TextView(this)
                    desc.text = primaryWeaopn.description

                    container.addView(kvView)
                    container.addView(desc)

                    val divider = MaterialDivider(this)
                    divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    container.addView(divider)

                    layout.addView(container)
                }
                gear.removingPrimaryWeapon().forEachIndexed { index, g ->
                    val container = LinearLayout(this)
                    container.setPadding(0, 8, 0, 0)
                    container.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    container.orientation = LinearLayout.VERTICAL

                    val kvView = KeyValueViewBuildable(this)
                    kvView.set(g.type, g.name, showDiv = false)

                    val desc = TextView(this)
                    desc.text = g.description

                    container.addView(kvView)
                    container.addView(desc)

                    val divider = MaterialDivider(this)
                    divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    container.addView(divider)

                    layout.addView(container)
                }
            }
        }
    }
}