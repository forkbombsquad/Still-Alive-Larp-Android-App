package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.models.primaryWeapon
import com.forkbombsquad.stillalivelarp.services.models.removingPrimaryWeapon
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

        SharedPrefsManager.shared.getGear().ifLet { gear ->
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