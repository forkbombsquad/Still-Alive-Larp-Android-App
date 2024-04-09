package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.EventCreateModel
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditEventActivity : NoStatusBarActivity() {

    private lateinit var title: TextInputEditText
    private lateinit var date: TextInputEditText
    private lateinit var startTime: TextInputEditText
    private lateinit var endTime: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var update: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.editevent_title)
        date = findViewById(R.id.editevent_date)
        startTime = findViewById(R.id.editevent_startTime)
        endTime = findViewById(R.id.editevent_endTime)
        description = findViewById(R.id.editevent_description)
        update = findViewById(R.id.editevent_update)

        update.setOnClick {
            DataManager.shared.selectedEvent.ifLet { event ->
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
                        isStarted = event.isStarted,
                        isFinished = event.isFinished
                    )
                    val updateEventRequest = AdminService.UpdateEvent()
                    lifecycleScope.launch {
                        updateEventRequest.successfulResponse(UpdateModelSP(eventUpdateModel)).ifLet({ _ ->
                            DataManager.shared.unrelaltedUpdateCallback()
                            AlertUtils.displaySuccessMessage(this@EditEventActivity,"Event Updated!") { _, _ ->
                                DataManager.shared.activityToClose?.finish()
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
        }

        buildView()
    }

    private fun buildView() {
        DataManager.shared.selectedEvent.ifLet { event ->
            title.setText(event.title)
            date.setText(event.date)
            startTime.setText(event.startTime)
            endTime.setText(event.endTime)
            description.setText(event.description)
        }
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