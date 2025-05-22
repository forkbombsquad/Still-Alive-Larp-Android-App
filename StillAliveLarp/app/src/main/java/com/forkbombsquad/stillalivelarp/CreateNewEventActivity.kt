package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.models.EventCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreateNewEventActivity : NoStatusBarActivity() {

    private lateinit var title: TextInputEditText
    private lateinit var date: TextInputEditText
    private lateinit var startTime: TextInputEditText
    private lateinit var endTime: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var submit: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_event)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.createevent_title)
        date = findViewById(R.id.createevent_date)
        startTime = findViewById(R.id.createevent_startTime)
        endTime = findViewById(R.id.createevent_endTime)
        description = findViewById(R.id.createevent_description)
        submit = findViewById(R.id.createevent_submit)

        submit.setOnClick {
            val valResults = validateFields()
            if (!valResults.hasError) {
                submit.setLoading(true)
                val eventCreateModel = EventCreateModel(
                    title = title.text.toString(),
                    description = description.text.toString(),
                    date = date.text.toString(),
                    startTime = startTime.text.toString(),
                    endTime = endTime.text.toString(),
                    isStarted = "FALSE",
                    isFinished = "FALSE"
                )
                val createEventRequest = AdminService.CreateEvent()
                lifecycleScope.launch {
                    createEventRequest.successfulResponse(CreateModelSP(eventCreateModel)).ifLet({ _ ->
                        OldDataManager.shared.unrelaltedUpdateCallback()
                        AlertUtils.displaySuccessMessage(this@CreateNewEventActivity,"Event Created!") { _, _ ->
                            finish()
                        }
                    }, {
                        submit.setLoading(false)
                    })
                }
            } else {
                AlertUtils.displayValidationError(this, valResults.getErrorMessages())
            }
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