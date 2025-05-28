package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService

import com.forkbombsquad.stillalivelarp.services.utils.UpdatePSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ChangePlayerPasswordActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var newPw: TextInputEditText
    private lateinit var confirmPw: TextInputEditText
    private lateinit var submit: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_player_password)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.changeplayerpw_title)
        newPw = findViewById(R.id.changeplayerpw_new)
        confirmPw = findViewById(R.id.changeplayerpw_confirm)
        submit = findViewById(R.id.changeplayerpw_submit)

        title.text = "Change Password For ${OldDataManager.shared.selectedPlayer?.fullName}"

        submit.setOnClick {
            if (checkPasswordsMatch()) {
                val validationRes = validateFields()
                if (!validationRes.hasError) {
                    submit.setLoading(true)
                    val updatePRequest = AdminService.UpdatePAdmin()
                    lifecycleScope.launch {
                        updatePRequest.successfulResponse(UpdatePSP(
                            playerId = OldDataManager.shared.selectedPlayer?.id ?: -1,
                            p = newPw.text.toString()
                        ))
                        .ifLet({
                            AlertUtils.displaySuccessMessage(this@ChangePlayerPasswordActivity, "Password Successfully Updated For ${OldDataManager.shared.selectedPlayer?.fullName ?: ""}") { _, _ ->
                                OldDataManager.shared.activityToClose?.finish()
                                finish()
                            }
                        }, {
                            submit.setLoading(false)
                        })
                    }

                } else {
                    AlertUtils.displayValidationError(this, validationRes.getErrorMessages())
                }
            } else {
                AlertUtils.displayValidationError(this, "Passwords do not match")
            }
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(ValidationGroup(newPw, ValidationType.PASSWORD)))
    }

    private fun checkPasswordsMatch(): Boolean {
        return newPw.text.toString() == confirmPw.text.toString()
    }
}