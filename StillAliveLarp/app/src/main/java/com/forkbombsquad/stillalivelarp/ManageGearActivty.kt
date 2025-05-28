package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService

import com.forkbombsquad.stillalivelarp.services.models.GearCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.GearCell
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ManageGearActivty : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var progressbar: ProgressBar
    private lateinit var innerLayout: LinearLayout
    private lateinit var outerLayout: LinearLayout
    private lateinit var addNew: NavArrowButtonGreen
    private lateinit var submitButton: LoadingButton

    private var gearModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_gear)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.managegear_title)
        progressbar = findViewById(R.id.managegear_progressbar)
        innerLayout = findViewById(R.id.managegear_innerLayout)
        outerLayout = findViewById(R.id.managegear_outerLayout)
        addNew = findViewById(R.id.managegear_addNew)
        submitButton = findViewById(R.id.gear_submitButton)

        addNew.setOnClick {
            OldDataManager.shared.gearToEdit = null
            OldDataManager.shared.unrelaltedUpdateCallback = {
                gearModified = true
                buildView()
            }
            val intent = Intent(this, AddEditGearActivity::class.java)
            startActivity(intent)
        }

        submitButton.setOnClick {
            if (gearModified) {
                submitButton.setLoadingWithText("Organizing Gear...")
                val gear = OldDataManager.shared.selectedCharacterGear?.firstOrNull()
                if (gear != null) {
                    if (gear.id == -1) {
                        // Create New List
                        val createModel = GearCreateModel(gear.characterId, gear.gearJson)
                        val request = AdminService.CreateGear()
                        submitButton.setLoadingWithText("Creating Gear Listing...")
                        lifecycleScope.launch {
                            request.successfulResponse(CreateModelSP(createModel)).ifLet { newGearModel ->
                                OldDataManager.shared.selectedCharacterGear = arrayOf(newGearModel)
                                AlertUtils.displaySuccessMessage(this@ManageGearActivty, "Gear Listing Created!") { _, _ ->
                                    submitButton.setLoading(false)
                                    gearModified = false
                                    buildView()
                                }
                            }
                        }
                    } else {
                        // update Existing
                        val request = AdminService.UpdateGear()
                        submitButton.setLoadingWithText("Updating Gear...")
                        lifecycleScope.launch {
                            request.successfulResponse(UpdateModelSP(gear)).ifLet { updatedGearModel ->
                                OldDataManager.shared.selectedCharacterGear = arrayOf(updatedGearModel)
                                AlertUtils.displaySuccessMessage(this@ManageGearActivty, "Gear Changes Committed!") { _, _ ->
                                    submitButton.setLoading(false)
                                    gearModified = false
                                    buildView()
                                }
                            }
                        }
                    }
                }
            }
        }

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.SELECTED_CHARACTER_GEAR), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "Manage Gear For ${OldDataManager.shared.selectedChar?.fullName ?: ""}"
        if (OldDataManager.shared.loadingSelectedCharacterGear) {
            progressbar.isGone = false
            outerLayout.isGone = true
            submitButton.isGone = true
        } else {
            progressbar.isGone = true
            outerLayout.isGone = false
            innerLayout.isGone = false
            submitButton.isGone = !gearModified

            innerLayout.removeAllViews()
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
                innerLayout.addView(textView)

                list.forEach { g ->
                    val gearCell = GearCell(this)
                    gearCell.setup(g)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 8, 0, 8)
                    gearCell.layoutParams = params
                    gearCell.setOnClick {
                        OldDataManager.shared.gearToEdit = g
                        OldDataManager.shared.unrelaltedUpdateCallback = {
                            gearModified = true
                            buildView()
                        }
                        val intent = Intent(this, AddEditGearActivity::class.java)
                        startActivity(intent)
                    }
                    innerLayout.addView(gearCell)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (gearModified) {
            AlertUtils.displayYesNoMessage(this, "Are You Sure?", "You have unsaved changes. Are you sure you want to exit?", { _, _ ->
                super.onBackPressed()
            }, { _, _ -> })
        } else {
            super.onBackPressed()
        }
    }
}