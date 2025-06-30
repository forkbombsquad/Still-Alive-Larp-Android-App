package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
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

class EditEventActivity : NoStatusBarActivity() {

    private lateinit var title: TextInputEditText
    private lateinit var date: TextInputEditText
    private lateinit var startTime: TextInputEditText
    private lateinit var endTime: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var update: LoadingButton

    private lateinit var event: FullEventModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)
        setupView()
    }

    private fun setupView() {
        event = DataManager.shared.getPassedData(ManageEventActivity::class, DataManagerPassedDataKey.SELECTED_EVENT)!!

        title = findViewById(R.id.editevent_title)
        date = findViewById(R.id.editevent_date)
        startTime = findViewById(R.id.editevent_startTime)
        endTime = findViewById(R.id.editevent_endTime)
        description = findViewById(R.id.editevent_description)
        update = findViewById(R.id.editevent_update)

        update.setOnClick {
            val valResults = validateFields()
            if (!valResults.hasError) {
                update.setLoading(true)

                val eventUpdateModel = EventModel(
                    id = event.id,
                    title = title.text.toString(),
                    description = description.text.toString(),
                    date = date.text.toString(),
                    startTime = startTime.text.toString(),
                    endTime = endTime.text.toString(),
                    isStarted = event.isStarted.toString(),
                    isFinished = event.isFinished.toString()
                )
                val updateEventRequest = AdminService.UpdateEvent()
                lifecycleScope.launch {
                    updateEventRequest.successfulResponse(UpdateModelSP(eventUpdateModel)).ifLet({ _ ->
                        AlertUtils.displaySuccessMessage(this@EditEventActivity,"Event Updated!") { _, _ ->
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
                            finish()
                        }
                    }, {
                        update.setLoading(false)
                    })
                }
            } else {
                AlertUtils.displayValidationError(this, valResults.getErrorMessages())
            }
        }

        buildView()
    }

    private fun buildView() {
        title.setText(event.title)
        date.setText(event.date)
        startTime.setText(event.startTime)
        endTime.setText(event.endTime)
        description.setText(event.description)
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(
            arrayOf(
                ValidationGroup(title, ValidationType.TITLE),
                ValidationGroup(date, ValidationType.DATE),
                ValidationGroup(startTime, ValidationType.START_TIME),
                ValidationGroup(endTime, ValidationType.END_TIME),
                ValidationGroup(description, ValidationType.DESCRIPTION)
            )
        )
    }
}