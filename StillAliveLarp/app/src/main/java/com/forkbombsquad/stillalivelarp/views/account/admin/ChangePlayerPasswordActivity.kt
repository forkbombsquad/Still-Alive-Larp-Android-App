package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.views.shared.PlayersListActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel

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

    private lateinit var player: FullPlayerModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_player_password)
        setupView()
    }

    private fun setupView() {
        player = DataManager.shared.getPassedData(PlayersListActivity::class, DataManagerPassedDataKey.SELECTED_PLAYER)!!

        title = findViewById(R.id.changeplayerpw_title)
        newPw = findViewById(R.id.changeplayerpw_new)
        confirmPw = findViewById(R.id.changeplayerpw_confirm)
        submit = findViewById(R.id.changeplayerpw_submit)

        submit.setOnClick {
            if (checkPasswordsMatch()) {
                val validationRes = validateFields()
                if (!validationRes.hasError) {
                    submit.setLoading(true)
                    val updatePRequest = AdminService.UpdatePAdmin()
                    lifecycleScope.launch {
                        updatePRequest.successfulResponse(UpdatePSP(
                            playerId = player.id,
                            p = newPw.text.toString()
                        ))
                        .ifLet({
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            AlertUtils.displaySuccessMessage(this@ChangePlayerPasswordActivity, "Password Successfully Updated For ${player.fullName}") { _, _ ->
                                DataManager.shared.closeActiviesToClose()
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

        buildView()
    }

    private fun buildView() {
        title.text = "Change Password For ${player.fullName}"
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(ValidationGroup(newPw, ValidationType.PASSWORD)))
    }

    private fun checkPasswordsMatch(): Boolean {
        return newPw.text.toString() == confirmPw.text.toString()
    }
}