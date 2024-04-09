package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.PlayerManager
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
import com.forkbombsquad.stillalivelarp.services.models.PlayerCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.PlayerCreateSP
import com.forkbombsquad.stillalivelarp.utils.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreateAccountActivity : NoStatusBarActivity() {

    private lateinit var fullNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var preApprovalCodeField: EditText

    private lateinit var submitButton: LoadingButton
    private lateinit var contactButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        StillAliveLarpApplication.activity = this

        fullNameField = findViewById(R.id.full_name_edit_text)
        emailField = findViewById(R.id.email_edit_text)
        passwordField = findViewById(R.id.password_edit_text)
        confirmPasswordField = findViewById(R.id.password_confirm_edit_text)
        preApprovalCodeField = findViewById(R.id.preapproval_security_code_edit_text)

        submitButton = findViewById<LoadingButton>(R.id.submit_button)
        contactButton = findViewById<LoadingButton>(R.id.contact_us_button)

        submitButton.setOnClick {
            submitButton.setLoading(true)
            if (checkPasswordsMatch()) {
                val valResult = validateFields()
                if (!valResult.hasError) {
                    val createPlayerReqeuest = PlayerService.CreatePlayer()
                    val newPlayer = PlayerCreateModel(
                        username = emailField.text.toString(),
                        fullName = fullNameField.text.toString(),
                        startDate = LocalDate.now().yyyyMMddFormatted(),
                        experience = "0",
                        freeTier1Skills = "0",
                        prestigePoints = "0",
                        isCheckedIn = "FALSE",
                        isCheckedInAsNpc = "FALSE",
                        lastCheckIn = "",
                        numEventsAttended = "0",
                        numNpcEventsAttended = "0",
                        isAdmin = "FALSE",
                        password = passwordField.text.toString()
                    )
                    lifecycleScope.launch {
                        createPlayerReqeuest.successfulResponse(PlayerCreateSP(preapprovalcode = preApprovalCodeField.text.toString(), player = newPlayer)).ifLet({
                            runOnUiThread {
                                PlayerManager.shared.setPlayer(it)
                                SharedPrefsManager.shared.clearAll(globalGetContext())
                                UserAndPassManager.shared.clear(globalGetContext())
                                DataManager.forceReset()
                                UserAndPassManager.shared.setTemp(globalGetContext(), emailField.text.toString(), passwordField.text.toString())
                                AlertUtils.displayOkMessage(this@CreateAccountActivity, "Success!", "Account for ${emailField.text.toString()} created!") { _, _ ->
                                    runOnUiThread {
                                        finish()
                                    }
                                }
                            }

                        }, {
                            submitButton.setLoading(false)
                        })
                    }
                } else {
                    AlertUtils.displayValidationError(this, valResult.getErrorMessages())
                    submitButton.setLoading(false)
                }
            } else {
                AlertUtils.displayOkMessage(this, "Validation Error", "Passwords do not match!")
                submitButton.setLoading(false)
            }
        }

        contactButton.setOnClick {
            val intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(
            ValidationGroup(fullNameField, ValidationType.FULL_NAME),
            ValidationGroup(emailField, ValidationType.EMAIL),
            ValidationGroup(passwordField, ValidationType.PASSWORD),
            ValidationGroup(confirmPasswordField, ValidationType.PASSWORD),
            ValidationGroup(preApprovalCodeField, ValidationType.SECURITY_CODE)))
    }

    private fun checkPasswordsMatch(): Boolean {
        return passwordField.text.toString() == confirmPasswordField.text.toString()
    }

}