package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.PlayerService

import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.RefundSkillSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.launch

class RefundSkillsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var layout: LinearLayout
    private lateinit var loadingBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund_skills)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.refundskills_title)
        layout = findViewById(R.id.refundskills_layout)
        loadingBar = findViewById(R.id.refundskills_progressbar)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.FULL_CHARACTER_FOR_SELECTED_CHARACTER), false) {
            buildView()
        }
        buildView()
    }
    private fun buildView() {
        title.text = "${OldDataManager.shared.selectedChar?.fullName ?: ""}'s\nRefundable Skills"
        loadingBar.isGone = !OldDataManager.shared.loadingFullCharForSelectedChar
        layout.removeAllViews()
        OldDataManager.shared.fullCharForSelectedChar.ifLet { char ->
            char.skills.toList().filter { it.xpCost.toInt() != 0 }.alphabetized().forEachIndexed { index, skill ->
                val arrow = NavArrowButtonBlackBuildable(this)
                arrow.textView.text = skill.name
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    arrow.setLoading(true)
                    AlertUtils.displayOkCancelMessage(this@RefundSkillsActivity, "Are you sure?", "Refund ${skill.name} to ${char.fullName}?", onClickOk = { _, _ ->
                        val deleteSkillRequest = AdminService.DeleteCharacterSkill()
                        lifecycleScope.launch {
                            deleteSkillRequest.successfulResponse(RefundSkillSP(char.playerId, char.id, skill.id)).ifLet({ deletedSkills ->
                                val playerRequest = PlayerService.GetPlayer()
                                lifecycleScope.launch {
                                    playerRequest.successfulResponse(IdSP(char.playerId)).ifLet({ fullPlayer ->
                                        var xp = 0
                                        var fs = 0
                                        var pp = 0
                                        for (skl in deletedSkills.charSkills) {
                                            xp += skl.xpSpent
                                            fs += skl.fsSpent
                                            pp += skl.ppSpent
                                        }
                                        val playerUpdate = PlayerModel(
                                            id = fullPlayer.id,
                                            username = fullPlayer.username,
                                            fullName =  fullPlayer.fullName,
                                            startDate = fullPlayer.startDate,
                                            experience = (fullPlayer.experience.toInt() + xp).toString(),
                                            freeTier1Skills = (fullPlayer.freeTier1Skills.toInt() + fs).toString(),
                                            prestigePoints = (fullPlayer.prestigePoints.toInt() + pp).toString(),
                                            isCheckedIn = fullPlayer.isCheckedIn,
                                            isCheckedInAsNpc = fullPlayer.isCheckedInAsNpc,
                                            lastCheckIn = fullPlayer.lastCheckIn,
                                            numEventsAttended = fullPlayer.numEventsAttended,
                                            numNpcEventsAttended = fullPlayer.numNpcEventsAttended,
                                            isAdmin = fullPlayer.isAdmin
                                        )
                                        val playerUpdateRequest = AdminService.UpdatePlayer()
                                        lifecycleScope.launch {
                                            playerUpdateRequest.successfulResponse(UpdateModelSP(playerUpdate)).ifLet({ _ ->
                                                AlertUtils.displayOkMessage(this@RefundSkillsActivity, "Success!", "Refunded ${xp}xp, ${fs}fs, and ${pp}pp to ${char.fullName} (${fullPlayer.fullName})!") { _, _ ->
                                                    OldDataManager.shared.loadingFullCharForSelectedChar = true
                                                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.FULL_CHARACTER_FOR_SELECTED_CHARACTER), false) {
                                                        buildView()
                                                    }
                                                    buildView()
                                                    arrow.setLoading(false)
                                                }
                                            }, {
                                                arrow.setLoading(false)
                                                AlertUtils.displaySomethingWentWrong(this@RefundSkillsActivity)
                                            })
                                        }
                                    }, {
                                        arrow.setLoading(false)
                                        AlertUtils.displaySomethingWentWrong(this@RefundSkillsActivity)
                                    })
                                }
                            }, {
                                arrow.setLoading(false)
                                AlertUtils.displaySomethingWentWrong(this@RefundSkillsActivity)
                            })
                        }

                    }, onClickCancel = {  _, _ ->
                        arrow.setLoading(false)
                    })
                }
                layout.addView(arrow)
            }
        }
    }
}