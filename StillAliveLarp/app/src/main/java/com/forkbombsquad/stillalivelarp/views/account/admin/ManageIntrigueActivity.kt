package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.views.shared.EventsListActivity
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel

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
    private lateinit var layout: LinearLayout
    private lateinit var investigator: TextInputEditText
    private lateinit var interrogator: TextInputEditText
    private lateinit var submitUpdateButton: LoadingButton

    private lateinit var event: FullEventModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_intrigue)
        setupView()
    }

    private fun setupView() {
        event = DataManager.shared.getPassedData(EventsListActivity::class, DataManagerPassedDataKey.SELECTED_EVENT)!!

        title = findViewById(R.id.intriguemanagement_title)
        layout = findViewById(R.id.intriguemanagement_layout)
        investigator = findViewById(R.id.intriguemanagement_investigator)
        interrogator = findViewById(R.id.intriguemanagement_interrogator)
        submitUpdateButton = findViewById(R.id.intriguemanagement_submitUpdateButton)

        submitUpdateButton.setOnClick {
            val valResult = validateFields()
            if (!valResult.hasError) {
                submitUpdateButton.setLoading(true)
                event.intrigue.ifLet({ intrigue ->
                    val intrigueUpdate = IntrigueModel(
                        id = intrigue.id,
                        eventId = event.id,
                        investigatorMessage = investigator.text.toString(),
                        interrogatorMessage = interrogator.text.toString(),
                        webOfInformantsMessage = ""
                    )
                    val updateIntrigueRequest = AdminService.UpdateIntrigue()
                    lifecycleScope.launch {
                        updateIntrigueRequest.successfulResponse(UpdateModelSP(intrigueUpdate)).ifLet({ _ ->
                            AlertUtils.displaySuccessMessage(this@ManageIntrigueActivity, "Intrigue Updated!") { _, _ ->
                                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                DataManager.shared.closeActiviesToClose()
                                finish()
                            }
                        }, {
                            submitUpdateButton.setLoading(false)
                        })
                    }
                }, {
                    val intrigueUpdate = IntrigueCreateModel(
                        eventId = event.id,
                        investigatorMessage = investigator.text.toString(),
                        interrogatorMessage = interrogator.text.toString(),
                        webOfInformantsMessage = ""
                    )
                    val updateIntrigueRequest = AdminService.CreateIntrigue()
                    lifecycleScope.launch {
                        updateIntrigueRequest.successfulResponse(CreateModelSP(intrigueUpdate)).ifLet({ _ ->
                            AlertUtils.displaySuccessMessage(this@ManageIntrigueActivity, "Intrigue Created!") { _, _ ->
                                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                DataManager.shared.closeActiviesToClose()
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

        buildView()
    }

    private fun buildView() {
        layout.isGone = false
        event.intrigue.ifLet({ intrigue ->
            title.text = "Update Intrigue"
            investigator.setText(intrigue.investigatorMessage)
            interrogator.setText(intrigue.interrogatorMessage)
            submitUpdateButton.set("Update")
        }, {
            title.text = "Create Intrigue"
            submitUpdateButton.set("Submit")
        })
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