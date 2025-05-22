package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.services.models.IntrigueCreateModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ManageIntrigueActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var investigator: TextInputEditText
    private lateinit var interrogator: TextInputEditText
    private lateinit var submitUpdateButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_intrigue)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.intriguemanagement_title)
        progressBar = findViewById(R.id.intriguemanagement_progressbar)
        layout = findViewById(R.id.intriguemanagement_layout)
        investigator = findViewById(R.id.intriguemanagement_investigator)
        interrogator = findViewById(R.id.intriguemanagement_interrogator)
        submitUpdateButton = findViewById(R.id.intriguemanagement_submitUpdateButton)

        submitUpdateButton.setOnClick {
            val valResult = validateFields()
            if (!valResult.hasError) {
                submitUpdateButton.setLoading(true)
                OldDataManager.shared.intrigueForSelectedEvent.ifLet({ intrigue ->
                    val intrigueUpdate = IntrigueModel(
                        id = intrigue.id,
                        eventId = intrigue.eventId,
                        investigatorMessage = investigator.text.toString(),
                        interrogatorMessage = interrogator.text.toString(),
                        webOfInformantsMessage = ""
                    )
                    val updateIntrigueRequest = AdminService.UpdateIntrigue()
                    lifecycleScope.launch {
                        updateIntrigueRequest.successfulResponse(UpdateModelSP(intrigueUpdate)).ifLet({ _ ->
                            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.INTRIGUE_FOR_SELECTED_EVENT), true) {}
                            AlertUtils.displaySuccessMessage(this@ManageIntrigueActivity, "Intrigue Updated!") { _, _ ->
                                OldDataManager.shared.activityToClose?.finish()
                                finish()
                            }
                        }, {
                            submitUpdateButton.setLoading(false)
                        })
                    }
                }, {
                    val intrigueUpdate = IntrigueCreateModel(
                        eventId = OldDataManager.shared.selectedEvent?.id ?: -1,
                        investigatorMessage = investigator.text.toString(),
                        interrogatorMessage = interrogator.text.toString(),
                        webOfInformantsMessage = ""
                    )
                    val updateIntrigueRequest = AdminService.CreateIntrigue()
                    lifecycleScope.launch {
                        updateIntrigueRequest.successfulResponse(CreateModelSP(intrigueUpdate)).ifLet({ _ ->
                            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.INTRIGUE_FOR_SELECTED_EVENT), true) {}
                            AlertUtils.displaySuccessMessage(this@ManageIntrigueActivity, "Intrigue Created!") { _, _ ->
                                OldDataManager.shared.activityToClose?.finish()
                                finish()
                            }
                        }, {
                            submitUpdateButton.setLoading(false)
                        })
                    }
                })
            } else {
                AlertUtils.displayValidationError(this, valResult.getErrorMessages())
            }
        }

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.INTRIGUE_FOR_SELECTED_EVENT), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        if (OldDataManager.shared.loadingIntrigueForSelectedEvent) {
            progressBar.isGone = false
            layout.isGone = true
        } else {
            progressBar.isGone = true
            layout.isGone = false
            OldDataManager.shared.intrigueForSelectedEvent.ifLet({ intrigue ->
                title.text = "Update Intrigue"
                investigator.setText(intrigue.investigatorMessage)
                interrogator.setText(intrigue.interrogatorMessage)
                submitUpdateButton.set("Update")
            }, {
                title.text = "Create Intrigue"
                submitUpdateButton.set("Submit")
            })
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(
            arrayOf(
                ValidationGroup(investigator, ValidationType.INTRIGUE),
                ValidationGroup(interrogator, ValidationType.INTRIGUE)
            )
        )
    }
}