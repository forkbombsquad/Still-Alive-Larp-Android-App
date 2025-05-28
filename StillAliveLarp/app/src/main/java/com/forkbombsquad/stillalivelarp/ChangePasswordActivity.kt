package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.PlayerManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
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

class ChangePasswordActivity : NoStatusBarActivity() {

    private lateinit var currentPw: TextInputEditText
    private lateinit var newPw: TextInputEditText
    private lateinit var confirmPw: TextInputEditText
    private lateinit var submit: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        setupView()
    }

    private fun setupView() {
        currentPw = findViewById(R.id.changepw_current)
        newPw = findViewById(R.id.changepw_new)
        confirmPw = findViewById(R.id.changepw_confirm)
        submit = findViewById(R.id.changepw_submit)

        submit.setOnClick {
            if (checkOldPass()) {
                if (checkPasswordsMatch()) {
                    val validationRes = validateFields()
                    if (!validationRes.hasError) {
                        submit.setLoading(true)

                        val updatePRequest = PlayerService.UpdateP()
                        lifecycleScope.launch {
                            updatePRequest.successfulResponse(UpdatePSP(
                                playerId = OldDataManager.shared.player?.id ?: -1,
                                p = newPw.text.toString()
                            )).ifLet({
                                OldDataManager.shared.player = it
                                PlayerManager.shared.setPlayer(it)
                                UserAndPassManager.shared.setUandP(it.username, newPw.text.toString(), true)
                                AlertUtils.displayOkMessage(this@ChangePasswordActivity, "Success", "Password Updated") { _, _ ->
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
            } else {
                AlertUtils.displayValidationError(this, "Existing password incorrect")
            }
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(ValidationGroup(newPw, ValidationType.PASSWORD)))
    }

    private fun checkPasswordsMatch(): Boolean {
        return newPw.text.toString() == confirmPw.text.toString()
    }

    private fun checkOldPass(): Boolean {
        val current = currentPw.text.toString()
        return current == UserAndPassManager.shared.getP()
    }
}