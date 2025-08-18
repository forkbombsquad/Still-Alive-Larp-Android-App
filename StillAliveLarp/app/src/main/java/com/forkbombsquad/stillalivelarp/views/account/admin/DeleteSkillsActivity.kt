package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.views.shared.CharactersListActivity
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.services.utils.RefundSkillSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.shared.SkillsListActivity
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class DeleteSkillsActivity : NoStatusBarActivity() {

    enum class DeleteSkillsActivityActionType {
        REFUND_XP,
        JUST_DELETE
    }

    private lateinit var title: TextView
    private lateinit var layout: LinearLayout

    private lateinit var character: FullCharacterModel
    private lateinit var action: DeleteSkillsActivityActionType

    private lateinit var loadingLayout: LoadingLayout

    private val sourceClasses: List<KClass<*>> = listOf(CharactersListActivity::class, AdminPanelActivity::class, SkillsListActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund_skills)
        setupView()
    }

    private fun setupView() {
        loadingLayout = findViewById(R.id.loadinglayout)

        character = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.SELECTED_CHARACTER)!!
        action = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.ACTION) ?: DeleteSkillsActivityActionType.REFUND_XP

        title = findViewById(R.id.refundskills_title)
        layout = findViewById(R.id.refundskills_layout)

        buildView()
    }

    private fun reloadView() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            character = DataManager.shared.getCharacter(character.id)!!
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        title.text = (action == DeleteSkillsActivityActionType.REFUND_XP).ternary("Refund Skills For\n${character.fullName}", "Delete Skills For\n${character.fullName}")
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
                    AlertUtils.displayOkCancelMessage(this@DeleteSkillsActivity, "Are you sure?", (action == DeleteSkillsActivityActionType.REFUND_XP).ternary("Delete ${skill.name} from ${character.fullName} and refund xp to ${DataManager.shared.getPlayerForCharacter(character).fullName}?", "Delete ${skill.name} from ${character.fullName}?"), onClickOk = { _, _ ->
                        val deleteSkillRequest = CharacterSkillService.DeleteCharacterSkill()
                        lifecycleScope.launch {
                            deleteSkillRequest.successfulResponse(RefundSkillSP(character.playerId, character.id, skill.id)).ifLet({ deletedSkills ->
                                if (action == DeleteSkillsActivityActionType.REFUND_XP) {
                                    // Refund
                                    val player = DataManager.shared.getPlayerForCharacter(character)
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
                                            AlertUtils.displayOkMessage(this@DeleteSkillsActivity, "Success!", "Refunded ${xp}xp, ${fs}fs, and ${pp}pp to ${character.fullName} (${player.fullName})!") { _, _ ->
                                                arrow.setLoading(false)
                                                reloadView()
                                            }
                                        }, {
                                            arrow.setLoading(false)
                                            AlertUtils.displaySomethingWentWrong(this@DeleteSkillsActivity)
                                        })
                                    }
                                } else {
                                    // No Refund
                                    DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                    AlertUtils.displayOkMessage(this@DeleteSkillsActivity, "Success!", "${skill.name} removed from ${character.fullName}!") { _, _ ->
                                        arrow.setLoading(false)
                                        reloadView()
                                    }
                                }
                            }, {
                                arrow.setLoading(false)
                                AlertUtils.displaySomethingWentWrong(this@DeleteSkillsActivity)
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