package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager

import com.forkbombsquad.stillalivelarp.services.models.AnnouncementCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreateAnnouncementActivity : NoStatusBarActivity() {

    private lateinit var title: TextInputEditText
    private lateinit var message: TextInputEditText
    private lateinit var submitButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_announcement)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.createannouncement_announcementTitle)
        message = findViewById(R.id.createannouncement_message)
        submitButton = findViewById(R.id.createannouncement_submitButton)

        submitButton.setOnClick {
            val valResult = validateFields()
            if (!valResult.hasError) {
                submitButton.setLoading(true)
                val announcementCreateModel = AnnouncementCreateModel(
                    title = title.text.toString(),
                    text = message.text.toString(),
                    date = LocalDate.now().yyyyMMddFormatted()
                )
                val createAnnouncementRequest = AdminService.CreateAnnouncement()
                lifecycleScope.launch {
                    createAnnouncementRequest.successfulResponse(CreateModelSP(announcementCreateModel)).ifLet({ _ ->
                        AlertUtils.displaySuccessMessage(this@CreateAnnouncementActivity, "Announcement Created") { _, _ ->
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            finish()
                        }
                    }, {
                        submitButton.setLoading(false)
                    })
                }
            } else {
                AlertUtils.displayValidationError(this, valResult.getErrorMessages())
            }
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(
            arrayOf(
                ValidationGroup(title, ValidationType.ANNOUNCEMENT_TITLE),
                ValidationGroup(message, ValidationType.ANNOUNCEMENT_MESSAGE)
            )
        )
    }
}