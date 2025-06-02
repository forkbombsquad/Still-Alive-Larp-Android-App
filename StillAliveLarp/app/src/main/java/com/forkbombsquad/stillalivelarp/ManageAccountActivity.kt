package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AwardService
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.EventAttendeeService
import com.forkbombsquad.stillalivelarp.services.EventPreregService
import com.forkbombsquad.stillalivelarp.services.GearService
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.ProfileImageService
import com.forkbombsquad.stillalivelarp.services.SpecialClassXpReductionService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager

import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.globalForceResetAllPlayerData
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ManageAccountActivity : NoStatusBarActivity() {

    private lateinit var changePass: NavArrowButtonBlack
    private lateinit var deleteLocaldata: LoadingButton
    private lateinit var deleteAccount: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)
        setupView()
    }

    private fun setupView() {
        changePass = findViewById(R.id.manageaccount_changePass)
        deleteLocaldata = findViewById(R.id.manageaccount_deleteLocalData)
        deleteAccount = findViewById(R.id.manageaccount_deleteAccount)

        changePass.setOnClick {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        deleteLocaldata.setOnClick {
            deleteLocaldata.setLoading(true)
            AlertUtils.displayDeleteLocalDataCancelMessage(this, onClickOk = { _, _ ->
                this.startDeletingLocalData()
            }, onClickCancel = { _, _ ->
                deleteLocaldata.setLoading(false)
            })
        }

        deleteAccount.setOnClick {
            deleteAccount.setLoading(true)
            AlertUtils.displayDeleteAccountCancelMessage(this, onClickOk = { _, _ ->
                this.deleteCharSkills()
            }, onClickCancel = { _, _ ->
                deleteAccount.setLoading(false)
            })
        }
    }

    private fun startDeletingLocalData() {
        globalForceResetAllPlayerData()
        AlertUtils.displaySuccessMessage(this, "All local data has been deleted!") { _, _ ->
            deleteLocaldata.setLoading(false)
        }
    }

    private fun deleteCharSkills() {
        val chars = DataManager.shared.getCurrentPlayer()?.characters ?: listOf()
        var count = 0
        chars.forEach { char ->
            deleteAccount.setLoadingWithText("Deleting Skills For: ${char.fullName}")
            val deleteSkillsRequest = CharacterSkillService.DeleteCharacterSkills()
            lifecycleScope.launch {
                deleteSkillsRequest.successfulResponse(IdSP(char.id)).ifLet({ _ ->
                    count += 1
                    if (count == chars.size) {
                        this@ManageAccountActivity.deleteCharGear()
                    }
                }, {
                    count += 1
                    if (count == chars.size) {
                        this@ManageAccountActivity.deleteCharGear()
                    }
                })
            }
        }
        if (chars.isEmpty()) {
            this.deleteEventAttendees()
        }
    }

    private fun deleteCharGear() {
        val chars = DataManager.shared.getCurrentPlayer()?.characters ?: listOf()
        var count = 0
        chars.forEach { char ->
            deleteAccount.setLoadingWithText("Deleting Gear For: ${char.fullName}")
            val deleteGearRequest = GearService.DeleteGear()
            lifecycleScope.launch {
                deleteGearRequest.successfulResponse(IdSP(char.id)).ifLet({ _ ->
                    count += 1
                    if (count == chars.size) {
                        this@ManageAccountActivity.deleteSpecialClassXpReductions()
                    }
                }, {
                    count += 1
                    if (count == chars.size) {
                        this@ManageAccountActivity.deleteSpecialClassXpReductions()
                    }
                })
            }
        }
    }

    private fun deleteSpecialClassXpReductions() {
        val chars = DataManager.shared.getCurrentPlayer()?.characters ?: listOf()
        var count = 0
        chars.forEach { char ->
            deleteAccount.setLoadingWithText("Deleting Xp Reductions For: ${char.fullName}")
            val deleteXpRedsRequest = SpecialClassXpReductionService.DeleteXpReductionsForCharacter()
            lifecycleScope.launch {
                deleteXpRedsRequest.successfulResponse(IdSP(char.id)).ifLet({ _ ->
                    count += 1
                    if (count == chars.size) {
                        this@ManageAccountActivity.deleteEventAttendees()
                    }
                }, {
                    count += 1
                    if (count == chars.size) {
                        this@ManageAccountActivity.deleteEventAttendees()
                    }
                })
            }
        }
    }

    private fun deleteEventAttendees() {
        deleteAccount.setLoadingWithText("Deleting Event Attendees")
        val request = EventAttendeeService.DeleteEventAttendeesForPlayer()
        lifecycleScope.launch {
            request.successfulResponse().ifLet({ _ ->
                this@ManageAccountActivity.deleteAwards()
            }, {
                this@ManageAccountActivity.deleteAwards()
            })
        }
    }

    private fun deleteAwards() {
        deleteAccount.setLoadingWithText("Deleting Awards")
        val request = AwardService.DeleteAwardsForPlayer()
        lifecycleScope.launch {
            request.successfulResponse().ifLet({ _ ->
                this@ManageAccountActivity.deletePreregs()
            }, {
                this@ManageAccountActivity.deletePreregs()
            })
        }
    }

    private fun deletePreregs() {
        deleteAccount.setLoadingWithText("Deleting Preregistrations")
        val request = EventPreregService.DeletePreregistrationsForPlayer()
        lifecycleScope.launch {
            request.successfulResponse().ifLet({ _ ->
                this@ManageAccountActivity.deleteCharacters()
            }, {
                this@ManageAccountActivity.deleteCharacters()
            })
        }
    }

    private fun deleteCharacters() {
        deleteAccount.setLoadingWithText("Deleting Characters")
        val request = CharacterService.DeleteCharacters()
        lifecycleScope.launch {
            request.successfulResponse().ifLet({ _ ->
                this@ManageAccountActivity.deletePhotos()
            }, {
                this@ManageAccountActivity.deletePhotos()
            })
        }
    }

    private fun deletePhotos() {
        deleteAccount.setLoadingWithText("Deleting Profile Photos")
        val request = ProfileImageService.DeleteProfileImages()
        lifecycleScope.launch {
            request.successfulResponse(IdSP(DataManager.shared.getCurrentPlayer()?.id ?: -1)).ifLet({ _ ->
                this@ManageAccountActivity.deletePlayer()
            }, {
                this@ManageAccountActivity.deletePlayer()
            })
        }
    }

    private fun deletePlayer() {
        deleteAccount.setLoadingWithText("Deleting Player")
        val request = PlayerService.DeletePlayer()
        lifecycleScope.launch {
            request.successfulResponse().ifLet({ _ ->
                this@ManageAccountActivity.successDeleting()
            }, {
                deleteAccount.setLoading(false)
            })
        }
    }

    private fun successDeleting() {
        deleteAccount.setLoadingWithText("")
        deleteAccount.setLoading(true)
        AlertUtils.displaySuccessMessage(this, "Your account and all associated data has been deleted!") { _, _ ->
            globalForceResetAllPlayerData()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
}