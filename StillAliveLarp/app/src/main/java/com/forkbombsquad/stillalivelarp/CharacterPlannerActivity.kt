package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService

import com.forkbombsquad.stillalivelarp.services.models.CharacterCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlueBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreenBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import kotlinx.coroutines.launch
import java.time.LocalDate

class CharacterPlannerActivity : NoStatusBarActivity() {
    // TODO need to figure out swipe to delete

    private lateinit var title: TextView
    private lateinit var layout: LinearLayout

    private lateinit var loadingStuffLayout: LinearLayout
    private lateinit var loadingStuffText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_planner)
        setupView()
    }

    private fun setupView() {
        progressBar = findViewById(R.id.characterplanner_loading)
        layout = findViewById(R.id.characterplanner_layout)
        loadingStuffLayout = findViewById(R.id.characterplanner_loadingstufflayout)
        loadingStuffText = findViewById(R.id.characterplanner_loadingstufftext)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_PLANNED_CHARACTERS, OldDataManagerType.ALL_CHARACTERS), false) {
            allPersonalChars = (OldDataManager.shared.allCharacters?.filter { it.playerId == OldDataManager.shared.player?.id } ?: listOf()) + (OldDataManager.shared.allPlannedCharacters?.filter { it.playerId == OldDataManager.shared.player?.id } ?: listOf())
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        progressBar.isGone = !OldDataManager.shared.loadingAllPlannedCharacters
        loadingStuffLayout.isGone = true
        layout.removeAllViews()
        OldDataManager.shared.allPlannedCharacters.ifLet { chars ->
            chars.forEach { char ->
                val navarrow = NavArrowButtonBlueBuildable(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                navarrow.layoutParams = params
                navarrow.textView.text = char.fullName
                navarrow.setOnClick {
                    loadExisting(char)
                }
                navarrow.setLoading(loading)
                layout.addView(navarrow)
            }
            val navarrow = NavArrowButtonGreenBuildable(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 16, 0, 16)
            navarrow.layoutParams = params

            navarrow.textView.text = "Start A New Plan"
            navarrow.setLoading(loading)

            val choices: MutableList<String> = mutableListOf()
            choices.add("New Plan")
            allPersonalChars?.forEach {
                choices.add(it.fullName)
            }
            navarrow.setOnClick {
                AlertUtils.displayChoiceMessage(this, "Create a new plan or base one off of an existing Character?", choices.toTypedArray()) { selectedIndex ->
                    if (selectedIndex == 0) {
                        AlertUtils.displayMessageWithTextField(this, "Creating Plan") { name ->
                            createNew(name, null)
                        }
                    } else if (selectedIndex > 0) {
                        allPersonalChars?.get(selectedIndex - 1).ifLet { selectedChar ->
                            AlertUtils.displayMessageWithTextField(this, "Creating Plan") { name ->
                                createNew(name, selectedChar)
                            }
                        }
                    }
                }
            }
            layout.addView(navarrow)
        }
    }

    private fun setLoadingText(text: String?) {
        text.ifLet({ str ->
           runOnUiThread {
               loadingStuffText.text = str
               loadingStuffLayout.isGone = false
           }
        }, {
            runOnUiThread {
                loadingStuffLayout.isGone = true
            }
        })
    }

    private fun createNew(name: String, selectedChar: CharacterModel?) {
        loading = true
        buildView()
        setLoadingText("Creating Base Model...")
        var nm = name
        if (nm.isEmpty()) {
            selectedChar.ifLet({
                nm = it.fullName + " plan"
            }, {
                nm = "Planned Character"
            })
        }
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
            playerId = OldDataManager.shared.player?.id ?: 0,
            characterTypeId = CharacterType.PLANNER.id
        )

        val request = CharacterService.CreatePlannedCharacter()
        lifecycleScope.launch {
            request.successfulResponse(CharacterCreateSP(createModel)).ifLet { createdChar ->
                selectedChar.ifLet({ oldChar ->
                    addSkillsFromExisting(createdChar, oldChar) {
                        setLoadingText("Loading New Planed Character...")
                        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_PLANNED_CHARACTERS, OldDataManagerType.ALL_CHARACTERS), true) {
                            allPersonalChars = (OldDataManager.shared.allCharacters?.filter { it.playerId == OldDataManager.shared.player?.id } ?: listOf()) + (OldDataManager.shared.allPlannedCharacters?.filter { it.playerId == OldDataManager.shared.player?.id } ?: listOf())
                            buildView()
                            loadExisting(createdChar)
                        }
                    }
                }, {
                    setLoadingText("Loading New Planed Character...")
                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_PLANNED_CHARACTERS, OldDataManagerType.ALL_CHARACTERS), true) {
                        allPersonalChars = (OldDataManager.shared.allCharacters?.filter { it.playerId == OldDataManager.shared.player?.id } ?: listOf()) + (OldDataManager.shared.allPlannedCharacters?.filter { it.playerId == OldDataManager.shared.player?.id } ?: listOf())
                        buildView()
                        loadExisting(createdChar)
                    }
                })
            }
        }
    }

    private fun addSkillsFromExisting(newChar: CharacterModel, existingChar: CharacterModel, finished: () -> Unit) {
        loading = true
        val request = CharacterSkillService.GetAllCharacterSkillsForCharacter()
        lifecycleScope.launch {
            request.successfulResponse(IdSP(existingChar.id)).ifLet {  list ->
                val nonZeros = list.charSkills.filter { it.xpSpent > 0 || it.fsSpent > 0 }.toList()
                var count = 0
                setLoadingText("Populating Skills (0 / ${nonZeros.size})...")
                nonZeros.forEach { nzs ->
                    val charSkill = CharacterSkillCreateModel(
                        characterId = newChar.id,
                        skillId = nzs.skillId,
                        xpSpent = nzs.xpSpent,
                        fsSpent = nzs.fsSpent,
                        ppSpent = nzs.ppSpent
                    )
                    val csRequest = CharacterSkillService.TakePlannedCharacterSkill()
                    lifecycleScope.launch {
                        csRequest.successfulResponse(CreateModelSP(charSkill)).ifLet { _ ->
                            count ++
                            setLoadingText("Populating Skills (${count} / ${nonZeros.size})...")
                            if (count == nonZeros.size) {
                                finished()
                            }
                        }
                    }
                }
            }
        }
    }
    private fun loadExisting(character: CharacterModel) {
        loading = true
        buildView()
        CharacterManager.shared.fetchFullCharacter(lifecycleScope, character.id) { fullCharacter ->
            OldDataManager.shared.selectedPlannedCharacter = fullCharacter
            OldDataManager.shared.unrelaltedUpdateCallback = {
                loading = true
                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_PLANNED_CHARACTERS, OldDataManagerType.ALL_CHARACTERS), forceDownloadIfApplicable = true) {
                    loading = false
                    allPersonalChars = OldDataManager.shared.allCharacters?.filter { it.playerId == OldDataManager.shared.player?.id }
                    buildView()
                }
                buildView()
            }
            val request = CharacterSkillService.GetAllCharacterSkillsForCharacter()
            lifecycleScope.launch {
                request.successfulResponse(IdSP(character.id)).ifLet { charSkills ->
                    runOnUiThread {
                        OldDataManager.shared.selectedPlannedCharacterCharSkills = charSkills.charSkills.toList()
                        loading = false
                        buildView()
                        val intent = Intent(this@CharacterPlannerActivity, CharacterPlannerSkillListActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}