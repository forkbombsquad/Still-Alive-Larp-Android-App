package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.AwardCharType
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import com.forkbombsquad.stillalivelarp.views.account.admin.AdminPanelActivity
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.forkbombsquad.stillalivelarp.views.account.admin.ManageEventActivity
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.reflect.KClass

class CharactersListActivity : NoStatusBarActivity() {

    class ACTIONS {
        companion object {
            const val AWARD_MVP = "awardmvp"
        }
    }

    private lateinit var title: TextView
    private lateinit var innerLayout: LinearLayout

    private var action: String? = null
    private lateinit var destClass: KClass<*>
    private lateinit var characters: List<FullCharacterModel>
    private lateinit var viewTitle: String

    private val sourceClasses: List<KClass<*>> = listOf(ViewPlayerActivity::class, MyAccountFragment::class, AdminPanelActivity::class, ManageEventActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_characters_list)
        setupView()
    }

    private fun setupView() {
        action = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.ACTION)
        if (action == null) {
            destClass = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.DESTINATION_CLASS)!!
        }
        characters = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.CHARACTER_LIST)!!
        viewTitle = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.VIEW_TITLE)!!

        title = findViewById(R.id.charlist_title)
        innerLayout = findViewById(R.id.charlist_innerlayout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, viewTitle)
        innerLayout.removeAllViews()
        characters.alphabetized().forEachIndexed { index, character ->
            val playerName = DataManager.shared.getPlayerForCharacter(character).fullName
            val arrow = NavArrowButtonBlackBuildable(this)
            arrow.textView.text = "${character.fullName} - ($playerName)"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                when (action) {
                    ACTIONS.AWARD_MVP -> {
                        arrow.setLoading(true)
                        AlertUtils.displayYesNoMessage(this@CharactersListActivity, "Award MVP?", "Award 1 of each material to ${character.fullName}?", onClickYes = {  _, _ ->
                            arrow.setLoading(true)
                            awardMVP(character)
                        }, onClickNo = { _, _ ->
                            arrow.setLoading(false)
                        })
                    }
                    else -> {
                        // Dest Class
                        DataManager.shared.addActivityToClose(this)
                        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
                        val intent = Intent(this, destClass.java)
                        startActivity(intent)
                    }
                }
            }
            innerLayout.addView(arrow)
        }
    }

    private fun awardMVP(character: FullCharacterModel) {
        var finishedCount = 0
        val completion = {
            finishedCount += 1
            if (finishedCount >= 6) {
                DataManager.shared.load(lifecycleScope) {
                    AlertUtils.displaySuccessMessage(this@CharactersListActivity, "Awarded ${character.fullName} 1 of every Material for MVP!") { _, _ ->
                        finish()
                    }
                }
            }
        }
        awardMPVWithAwardType(character, AwardCharType.MATERIALCASINGS, completion)
        awardMPVWithAwardType(character, AwardCharType.MATERIALCLOTH, completion)
        awardMPVWithAwardType(character, AwardCharType.MATERIALMED, completion)
        awardMPVWithAwardType(character, AwardCharType.MATERIALMETAL, completion)
        awardMPVWithAwardType(character, AwardCharType.MATERIALWOOD, completion)
        awardMPVWithAwardType(character, AwardCharType.MATERIALTECH, completion)

    }

    private fun awardMPVWithAwardType(character: FullCharacterModel, awardType: AwardCharType, completion: () -> Unit) {
        val awardCreateModel = AwardCreateModel.createCharacterAward(
            char = character.baseModel(),
            awardType = awardType,
            reason = "MVP of the Event ${LocalDate.now().yyyyMMddFormatted().yyyyMMddToMonthDayYear()}",
            amount = "1"
        )
        val awardCharRequest = AdminService.AwardCharacter()
        lifecycleScope.launch {
            awardCharRequest.successfulResponse(AwardCreateSP(awardCreateModel)).ifLet({ _ ->
                AlertUtils.displaySuccessMessage(this@CharactersListActivity, "Successfully Awarded ${character.fullName}!") { _, _ ->
                    completion()
                }
            }, {
                completion()
            })
        }
    }
}