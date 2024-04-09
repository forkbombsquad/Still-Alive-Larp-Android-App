package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.IntrigueCreateModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ManageIntrigueActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var investigator: TextInputEditText
    private lateinit var interrogator: TextInputEditText
    private lateinit var web: TextInputEditText
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
        web = findViewById(R.id.intriguemanagement_web)
        submitUpdateButton = findViewById(R.id.intriguemanagement_submitUpdateButton)

        submitUpdateButton.setOnClick {
            val valResult = validateFields()
            if (!valResult.hasError) {
                submitUpdateButton.setLoading(true)
                DataManager.shared.intrigueForSelectedEvent.ifLet({ intrigue ->
                    val intrigueUpdate = IntrigueModel(
                        id = intrigue.id,
                        eventId = intrigue.eventId,
                        investigatorMessage = investigator.text.toString(),
                        interrogatorMessage = interrogator.text.toString(),
                        webOfInformantsMessage = web.text.toString()
                    )
                    val updateIntrigueRequest = AdminService.UpdateIntrigue()
                    lifecycleScope.launch {
                        updateIntrigueRequest.successfulResponse(UpdateModelSP(intrigueUpdate)).ifLet({ _ ->
                            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.INTRIGUE_FOR_SELECTED_EVENT), true) {}
                            AlertUtils.displaySuccessMessage(this@ManageIntrigueActivity, "Intrigue Updated!") { _, _ ->
                                DataManager.shared.activityToClose?.finish()
                                finish()
                            }
                        }, {
                            submitUpdateButton.setLoading(false)
                        })
                    }
                }, {
                    val intrigueUpdate = IntrigueCreateModel(
                        eventId = DataManager.shared.selectedEvent?.id ?: -1,
                        investigatorMessage = investigator.text.toString(),
                        interrogatorMessage = interrogator.text.toString(),
                        webOfInformantsMessage = web.text.toString()
                    )
                    val updateIntrigueRequest = AdminService.CreateIntrigue()
                    lifecycleScope.launch {
                        updateIntrigueRequest.successfulResponse(CreateModelSP(intrigueUpdate)).ifLet({ _ ->
                            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.INTRIGUE_FOR_SELECTED_EVENT), true) {}
                            AlertUtils.displaySuccessMessage(this@ManageIntrigueActivity, "Intrigue Created!") { _, _ ->
                                DataManager.shared.activityToClose?.finish()
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

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.INTRIGUE_FOR_SELECTED_EVENT), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        if (DataManager.shared.loadingIntrigueForSelectedEvent) {
            progressBar.isGone = false
            layout.isGone = true
        } else {
            progressBar.isGone = true
            layout.isGone = false
            DataManager.shared.intrigueForSelectedEvent.ifLet({ intrigue ->
                title.text = "Update Intrigue"
                investigator.setText(intrigue.investigatorMessage)
                interrogator.setText(intrigue.interrogatorMessage)
                web.setText(intrigue.webOfInformantsMessage)
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
                ValidationGroup(interrogator, ValidationType.INTRIGUE),
                ValidationGroup(web, ValidationType.INTRIGUE)
            )
        )
    }
}