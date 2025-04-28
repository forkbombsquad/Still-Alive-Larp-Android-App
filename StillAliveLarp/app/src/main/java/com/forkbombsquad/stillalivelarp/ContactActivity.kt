package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.ContactRequestService
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.ContactCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ContactActivity : NoStatusBarActivity() {

    private lateinit var fullName: EditText
    private lateinit var email: EditText
    private lateinit var postalCode: EditText
    private lateinit var message: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        StillAliveLarpApplication.activity = this

        fullName = findViewById(R.id.contact_full_name_edit_text)
        email = findViewById(R.id.contact_email_edit_text)
        postalCode = findViewById(R.id.contact_postal_code_edit_text)
        message = findViewById(R.id.contact_message_edit_text)

        val contactButton = findViewById<LoadingButton>(R.id.submit_contact_button)
        contactButton.setOnClick {
            contactButton.setLoading(true)
            val fieldValidation = validateFields()
            if (!fieldValidation.hasError) {
                val contactRequestRequest = ContactRequestService.CreateContactRequest()
                val contactRequest = ContactRequestCreateModel(
                    fullName = fullName.text.toString(),
                    emailAddress = email.text.toString(),
                    postalCode = postalCode.text.toString(),
                    message = message.text.toString(),
                    read = "FALSE"
                )
                lifecycleScope.launch {
                    contactRequestRequest.successfulResponse(ContactCreateSP(contactRequest)).ifLet({
                        AlertUtils.displayOkMessage(this@ContactActivity, "Success", "Contact Request Sent!") { _, _ ->
                            finish()
                        }
                    }, {
                        contactButton.setLoading(false)
                    })
                }
            } else {
                AlertUtils.displayValidationError(this, fieldValidation.getErrorMessages())
                contactButton.setLoading(false)
            }
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(
            ValidationGroup(fullName, ValidationType.FULL_NAME),
            ValidationGroup(email, ValidationType.EMAIL),
            ValidationGroup(postalCode, ValidationType.POSTAL_CODE),
            ValidationGroup(message, ValidationType.MESSAGE)
        ))
    }
}