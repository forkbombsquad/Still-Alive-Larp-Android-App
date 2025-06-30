package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ApproveBioActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var bio: TextView
    private lateinit var checkbox: CheckBox
    private lateinit var approve: LoadingButton
    private lateinit var deny: LoadingButton

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_bio)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(CharactersListActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.approvebio_title)
        bio = findViewById(R.id.approvebio_bio)
        checkbox = findViewById(R.id.approvebio_checkbox)
        approve = findViewById(R.id.approvebio_approvebutton)
        deny = findViewById(R.id.approvebio_denybutton)

        approve.setOnClick {
            approve.setLoading(true)
            deny.setLoading(true)
            val updateCharRequest = AdminService.UpdateCharacter()
            val charUpdate = character.baseModel()
            charUpdate.approvedBio = "TRUE"
            lifecycleScope.launch {
                updateCharRequest.successfulResponse(UpdateModelSP(charUpdate)).ifLet({
                    if (checkbox.isChecked) {
                        val awardPlayerRequest = AdminService.AwardPlayer()
                        val award = AwardCreateModel.createPlayerAward(
                            playerId = character.playerId,
                            awardType = AwardPlayerType.XP,
                            reason = "Bio approved",
                            amount = "1"
                        )
                        lifecycleScope.launch {
                            awardPlayerRequest.successfulResponse(AwardCreateSP(award)).ifLet({
                                AlertUtils.displayOkMessage(this@ApproveBioActivity, "Success", "Bio for ${charUpdate.fullName} approved and 1 xp was granted.") { _, _ ->
                                    DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                    DataManager.shared.closeActiviesToClose()
                                    finish()
                                }
                            }, {
                                AlertUtils.displayOkMessage(this@ApproveBioActivity, "Success?", "Bio for ${charUpdate.fullName} approved but something went wrong when granting xp.") { _, _ ->
                                    DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                    DataManager.shared.closeActiviesToClose()
                                    finish()
                                }
                            })
                        }
                    } else {
                        AlertUtils.displayOkMessage(this@ApproveBioActivity, "Success", "Bio for ${charUpdate.fullName} approved and no xp was granted.") { _, _ ->
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
                            finish()
                        }
                    }
                }, {
                    approve.setLoading(false)
                    deny.setLoading(false)
                })
            }
        }

        deny.setOnClick {
            AlertUtils.displayMessage(
                context = this,
                title = "Are You Sure?",
                message = "Are you sure you want to deny ${character.fullName}'s bio? This will completely remove it and they will have to write another one. If you only have a small qualm with it, just ask them to edit it, rather than denying it.",
                buttons = arrayOf(
                    AlertButton(
                        text = "Deny Bio",
                        onClick = { _, _ ->
                            approve.setLoading(true)
                            deny.setLoading(true)
                            val updateCharRequest = AdminService.UpdateCharacter()
                            val charUpdate = character.baseModel()
                            charUpdate.bio = ""
                            charUpdate.approvedBio = "FALSE"
                            lifecycleScope.launch {
                                updateCharRequest.successfulResponse(UpdateModelSP(charUpdate)).ifLet({
                                    AlertUtils.displayOkMessage(this@ApproveBioActivity, "Success", "Bio for ${charUpdate.fullName} denied") { _, _ ->
                                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                        DataManager.shared.closeActiviesToClose()
                                        finish()
                                    }
                                }, {
                                    approve.setLoading(false)
                                    deny.setLoading(false)
                                })
                            }
                        },
                        buttonType = ButtonType.NEGATIVE
                    ),
                    AlertButton(
                        text = "Cancel",
                        onClick = { _, _ -> },
                        buttonType = ButtonType.NEUTRAL
                    )
                )
            )
        }

        buildView()
    }

    private fun buildView() {
        title.text = "${character.fullName}'s\nBio"
        bio.text = character.bio
    }
}