package com.forkbombsquad.stillalivelarp.views.account

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.views.shared.SkillsListActivity
import com.forkbombsquad.stillalivelarp.views.shared.ViewPlayerActivity
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.services.models.CharacterCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlueSwipeable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreenBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import kotlinx.coroutines.launch
import java.time.LocalDate

class CharacterPlannerActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var instText: TextView
    private lateinit var layout: LinearLayout

    private lateinit var player: FullPlayerModel

    private lateinit var loadingLayout: LoadingLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_planner)
        setupView()
    }

    private fun setupView() {
        player = DataManager.shared.getPassedData(listOf(MyAccountFragment::class, ViewPlayerActivity::class), DataManagerPassedDataKey.SELECTED_PLAYER)!!

        loadingLayout = findViewById(R.id.loadinglayout)

        title = findViewById(R.id.plannedchars_title)
        instText = findViewById(R.id.characterplanner_instructions)
        layout = findViewById(R.id.characterplanner_layout)

        reload()
    }

    private fun reload(completed: () -> Unit = {}) {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            player = DataManager.shared.players.firstOrNull { player.id == it.id }!!
            buildView()
            completed()
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "${player.fullName}'s Planned Characters")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(instText, layout)) {
            if (!DataManager.shared.offlineMode) {
                instText.isGone = !DataManager.shared.playerIsCurrentPlayer(player)
            } else {
                instText.isGone = true
            }
            layout.isGone = false

            layout.removeAllViews()
            player.getPlannedCharacters().forEach { char ->
                val navarrow = NavArrowButtonBlueSwipeable(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                navarrow.layoutParams = params
                navarrow.textView.text = char.fullName
                navarrow.setOnClick {
                    loadExisting(char)
                }

                navarrow.setCanSwipe(false)

                if (!DataManager.shared.offlineMode) {
                    if (DataManager.shared.playerIsCurrentPlayer(player)) {
                        navarrow.setCanSwipe(true)
                        navarrow.setOnSwipe {
                            setLoading("Deleting Character...")
                            AlertUtils.displayYesNoMessage(this, "Are You Sure?", "Are you sure you want to delete the planned character: ${char.fullName}. Once deleted, it can never be recovered.", onClickYes = { _, _ ->
                                char.deleteCharacterDESTRUCTIVE(lifecycleScope) { success ->
                                    if (success) {
                                        AlertUtils.displayOkMessage(this, "Success", "${char.fullName} Successfully Deleted") { _, _ -> }
                                    }
                                    setLoading(false)
                                    reload()
                                }
                            }, onClickNo = { _, _ ->
                                setLoading(false)
                                buildView()
                            })
                        }
                    }
                }

                navarrow.setLoading(false)
                layout.addView(navarrow)
            }
            if (!DataManager.shared.offlineMode && DataManager.shared.playerIsCurrentPlayer(player)) {
                val navarrow = NavArrowButtonGreenBuildable(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                navarrow.layoutParams = params

                navarrow.textView.text = "Start A New Plan"

                val choices: MutableList<String> = mutableListOf()
                choices.add("New Plan")
                val chooseableChars = player.characters.filter { it.characterType() != CharacterType.NPC && it.characterType() != CharacterType.HIDDEN }
                chooseableChars.forEach {
                    var tagText = ""
                    when (it.characterType()) {
                        CharacterType.STANDARD -> {
                            tagText = "Standard Character"
                        }
                        CharacterType.PLANNER -> {
                            tagText = "Planned Character"
                        }
                        CharacterType.NPC, CharacterType.HIDDEN -> {}
                    }
                    choices.add("${it.fullName} (${tagText})")
                }
                navarrow.setOnClick {
                    AlertUtils.displayChoiceMessage(this, "Create a new plan or base one off of an existing Character?", choices.toTypedArray()) { selectedIndex ->
                        if (selectedIndex == 0) {
                            AlertUtils.displayMessageWithTextField(this, "Creating Plan") { name ->
                                createNew(name, null)
                            }
                        } else if (selectedIndex > 0) {
                            val selectedChar = chooseableChars[selectedIndex - 1]
                            AlertUtils.displayMessageWithTextField(this, "Creating Plan") { name ->
                                createNew(name, selectedChar)
                            }
                        }
                    }
                }
                navarrow.setLoading(false)
                layout.addView(navarrow)
            }
        }
    }

    private fun setLoading(boolean: Boolean) {
        loadingLayout.setLoading(boolean)
    }

    private fun setLoading(text: String) {
        setLoading(true)
        loadingLayout.setLoadingText(text, showGettingContent = false)
    }

    private fun createNew(name: String, selectedChar: FullCharacterModel?) {
        setLoading("Creating Base Model...")
        buildView()
        var nm = name
        if (nm.isEmpty()) {
            selectedChar.ifLet({
                nm = it.fullName
            }, {
                nm = "Planned Character"
            })
        }
        nm = player.getUniqueCharacterNameRec(nm)
        val createModel = CharacterCreateModel(
            fullName = nm,
            startDate = LocalDate.now().yyyyMMddFormatted(),
            isAlive = "TRUE",
            deathDate = "",
            infection = "0",
            bio = "",
            approvedBio = "FALSE",
            bullets = "20",
            megas = "0",
            rivals = "0",
            rockets = "0",
            bulletCasings = "0",
            clothSupplies = "0",
            woodSupplies = "0",
            metalSupplies = "0",
            techSupplies = "0",
            medicalSupplies = "0",
            armor = CharacterArmor.NONE.text,
            unshakableResolveUses = "0",
            mysteriousStrangerUses = "0",
            playerId = player.id,
            characterTypeId = CharacterType.PLANNER.id
        )

        val request = CharacterService.CreatePlannedCharacter()
        lifecycleScope.launch {
            request.successfulResponse(CharacterCreateSP(createModel)).ifLet { createdChar ->
                selectedChar.ifLet({ oldChar ->
                    addSkillsFromExisting(createdChar, oldChar) {
                        this@CharacterPlannerActivity.setLoading(false)
                        this@CharacterPlannerActivity.reload {
                            loadExisting(player.characters.first {it.id == createdChar.id })
                        }
                    }
                }, {
                    this@CharacterPlannerActivity.setLoading(false)
                    this@CharacterPlannerActivity.reload {
                        loadExisting(player.characters.first {it.id == createdChar.id })
                    }
                })
            }
        }
    }

    private fun addSkillsFromExisting(newChar: CharacterModel, existingChar: FullCharacterModel, finished: () -> Unit) {
        val nonZeros = existingChar.allPurchasedSkills().filter { it.spentXp() > 0 || it.spentFt1s() > 0 }.toList()
        var count = 0
        setLoading("Populating Skills (0 / ${nonZeros.size})...")
        nonZeros.forEach { nzs ->
            val charSkill = CharacterSkillCreateModel(
                characterId = newChar.id,
                skillId = nzs.id,
                xpSpent = nzs.spentXp(),
                fsSpent = nzs.spentFt1s(),
                ppSpent = nzs.spentPp()
            )
            val csRequest = CharacterSkillService.TakePlannedCharacterSkill()
            lifecycleScope.launch {
                csRequest.successfulResponse(CreateModelSP(charSkill)).ifLet { _ ->
                    count ++
                    setLoading("Populating Skills (${count} / ${nonZeros.size})...")
                    if (count == nonZeros.size) {
                        finished()
                    }
                }
            }
        }
    }
    private fun loadExisting(character: FullCharacterModel) {
        setLoading(false)
        reload {
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.ACTION, SkillsListActivity.SkillsListActivityActions.ALLOW_DELETE)
            val intent = Intent(this, SkillsListActivity::class.java)
            startActivity(intent)
        }
    }
}