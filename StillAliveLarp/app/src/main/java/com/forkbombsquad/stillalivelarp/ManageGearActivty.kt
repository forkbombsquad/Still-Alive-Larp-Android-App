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
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

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
        DataManager.shared.characterToEdit = DataManager.shared.getPassedData(CharactersListActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.managegear_title)
        progressbar = findViewById(R.id.managegear_progressbar)
        innerLayout = findViewById(R.id.managegear_innerLayout)
        outerLayout = findViewById(R.id.managegear_outerLayout)
        addNew = findViewById(R.id.managegear_addNew)
        submitButton = findViewById(R.id.gear_submitButton)

        addNew.setOnClick {
            DataManager.shared.gearToEdit = null
            DataManager.shared.setUpdateCallback(this::class) {
                gearModified = true
                buildView()
            }
            val intent = Intent(this, AddEditGearActivity::class.java)
            startActivity(intent)
        }

        submitButton.setOnClick {
            if (gearModified) {
                submitButton.setLoadingWithText("Organizing Gear...")
                val gear = DataManager.shared.characterToEdit!!.gear
                if (gear != null) {
                    if (gear.id == -1) {
                        // Create New List
                        val createModel = GearCreateModel(gear.characterId, gear.gearJson)
                        val request = AdminService.CreateGear()
                        submitButton.setLoadingWithText("Creating Gear Listing...")
                        lifecycleScope.launch {
                            request.successfulResponse(CreateModelSP(createModel)).ifLet { newGearModel ->
                                DataManager.shared.characterToEdit!!.gear = newGearModel
                                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
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
                                DataManager.shared.characterToEdit!!.gear = updatedGearModel
                                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
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

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Manage Gear For ${DataManager.shared.characterToEdit!!.fullName}")
        if (DataManager.shared.loading) {
            progressbar.isGone = false
            outerLayout.isGone = true
            submitButton.isGone = true
        } else {
            progressbar.isGone = true
            outerLayout.isGone = false
            innerLayout.isGone = false
            submitButton.isGone = !gearModified

            innerLayout.removeAllViews()
            val gearList = DataManager.shared.characterToEdit!!.getGearOrganized()
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
                    if (!DataManager.shared.offlineMode) {
                        gearCell.setOnClick {
                            DataManager.shared.gearToEdit = g
                            DataManager.shared.setUpdateCallback(this::class) {
                                gearModified = true
                                reload()
                            }
                            val intent = Intent(this, AddEditGearActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    innerLayout.addView(gearCell)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (gearModified) {
            AlertUtils.displayYesNoMessage(this, "Are You Sure?", "You have unsaved changes. Are you sure you want to exit?", { _, _ ->
                DataManager.shared.characterToEdit = null
                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                super.onBackPressed()
            }, { _, _ -> })
        } else {
            DataManager.shared.characterToEdit = null
            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
            super.onBackPressed()
        }
    }
}