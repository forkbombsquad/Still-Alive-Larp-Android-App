package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.RefundSkillSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.launch

class RefundSkillsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var layout: LinearLayout

    private lateinit var character: FullCharacterModel

    private lateinit var loadingLayout: LoadingLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund_skills)
        setupView()
    }

    private fun setupView() {
        loadingLayout = findViewById(R.id.loadinglayout)

        character = DataManager.shared.getPassedData(CharactersListActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.refundskills_title)
        layout = findViewById(R.id.refundskills_layout)

        buildView()
    }

    private fun reloadView() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        title.text = "${character.fullName}'s\nRefundable Skills"
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(layout)) {
            layout.removeAllViews()
            character.allPurchasedSkills().filter { it.baseXpCost() > 0 }.alphabetized().forEachIndexed { index, skill ->
                val arrow = NavArrowButtonBlackBuildable(this)
                arrow.textView.text = skill.name
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    arrow.setLoading(true)
                    AlertUtils.displayOkCancelMessage(this@RefundSkillsActivity, "Are you sure?", "Refund ${skill.name} to ${character.fullName}?", onClickOk = { _, _ ->
                        val deleteSkillRequest = AdminService.DeleteCharacterSkill()
                        lifecycleScope.launch {
                            deleteSkillRequest.successfulResponse(RefundSkillSP(character.playerId, character.id, skill.id)).ifLet({ deletedSkills ->
                                val player = DataManager.shared.players.firstOrNull { it.id == character.playerId }!!
                                var xp = 0
                                var fs = 0
                                var pp = 0
                                for (skl in deletedSkills.charSkills) {
                                    xp += skl.xpSpent
                                    fs += skl.fsSpent
                                    pp += skl.ppSpent
                                }
                                val playerUpdate = player.baseModelWithModifications(xp, fs, pp)
                                val playerUpdateRequest = AdminService.UpdatePlayer()
                                lifecycleScope.launch {
                                    playerUpdateRequest.successfulResponse(UpdateModelSP(playerUpdate)).ifLet({ _ ->
                                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                        AlertUtils.displayOkMessage(this@RefundSkillsActivity, "Success!", "Refunded ${xp}xp, ${fs}fs, and ${pp}pp to ${character.fullName} (${player.fullName})!") { _, _ ->
                                            arrow.setLoading(false)
                                            reloadView()
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