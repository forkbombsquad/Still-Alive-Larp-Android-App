package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.AnnouncementManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
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
                        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ANNOUNCEMENTS), true) { }
                        AlertUtils.displaySuccessMessage(this@CreateAnnouncementActivity, "Announcement Created") { _, _ ->
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