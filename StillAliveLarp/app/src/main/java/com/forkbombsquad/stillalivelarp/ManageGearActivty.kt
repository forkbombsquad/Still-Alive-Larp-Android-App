package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.removingPrimaryWeapon
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class ManageGearActivty : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var progressbar: ProgressBar
    private lateinit var innerLayout: LinearLayout
    private lateinit var outerLayout: LinearLayout
    private lateinit var addNew: NavArrowButtonGreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_gear)
        setupView()
    }

    // TODO just created Gear Cells. Use them.

    private fun setupView() {
        title = findViewById(R.id.managegear_title)
        progressbar = findViewById(R.id.managegear_progressbar)
        innerLayout = findViewById(R.id.managegear_innerLayout)
        outerLayout = findViewById(R.id.managegear_outerLayout)
        addNew = findViewById(R.id.managegear_addNew)

        addNew.setOnClick {
            DataManager.shared.selectedGear = null
            DataManager.shared.unrelaltedUpdateCallback = {
                DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SELECTED_CHARACTER_GEAR), true) {
                    buildView()
                }
                buildView()
            }
            val intent = Intent(this, AddEditGearActivity::class.java)
            startActivity(intent)
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SELECTED_CHARACTER_GEAR), true) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "Manage Gear For ${DataManager.shared.selectedChar?.fullName ?: ""}"
        if (DataManager.shared.loadingSelectedCharacterGear) {
            progressbar.isGone = false
            outerLayout.isGone = true
        } else {
            progressbar.isGone = true
            outerLayout.isGone = false
            innerLayout.isGone = false

            innerLayout.removeAllViews()

            DataManager.shared.selectedCharacterGear?.removingPrimaryWeapon().ifLet { gear ->
                gear.forEachIndexed { index, g ->
                    val arrow = NavArrowButtonBlackBuildable(this)
                    arrow.textView.text = "${g.name} - ${g.type}"
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                    arrow.layoutParams = params
                    arrow.setLoading(false)
                    arrow.setOnClick {
                        DataManager.shared.selectedGear = g
                        DataManager.shared.unrelaltedUpdateCallback = {
                            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SELECTED_CHARACTER_GEAR), true) {
                                buildView()
                            }
                            buildView()
                        }
                        // TODO need to add the gear to edit boi
                        val intent = Intent(this, AddEditGearActivity::class.java)
                        startActivity(intent)
                    }
                    innerLayout.addView(arrow)
                }
            }
        }
    }
}