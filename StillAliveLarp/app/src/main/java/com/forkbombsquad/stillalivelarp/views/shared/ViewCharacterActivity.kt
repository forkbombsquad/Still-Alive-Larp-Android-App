package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonTypePressed
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.DropdownSpinner
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.MessageInput
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.account.CharacterPlannerActivity
import kotlinx.coroutines.flow.combine
import kotlin.math.max

class ViewCharacterActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var playerName: KeyValueView
    private lateinit var loadingLayout: LoadingLayout
    private lateinit var viewStats: NavArrowButtonBlack
    private lateinit var viewSkillsTree: NavArrowButtonBlack
    private lateinit var viewSkillsList: NavArrowButtonBlack
    private lateinit var viewBio: NavArrowButtonBlack
    private lateinit var viewGear: NavArrowButtonBlack
    private lateinit var viewXpReductions: NavArrowButtonBlack
    private lateinit var viewAwards: NavArrowButtonBlack
    private lateinit var convertPlanToCharacter: NavArrowButtonGreen
    private lateinit var applyPlanToCharacter: NavArrowButtonGreen

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_character)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(listOf(CharactersListActivity::class, CharacterPlannerActivity::class), DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.viewchar_title)
        playerName = findViewById(R.id.viewchar_playerName)
        loadingLayout = findViewById(R.id.loadinglayout)
        viewStats = findViewById(R.id.charview_viewStats)
        viewSkillsTree = findViewById(R.id.charview_viewSkillsTree)
        viewSkillsList = findViewById(R.id.charview_viewSkillsList)
        viewBio = findViewById(R.id.charview_viewBio)
        viewGear = findViewById(R.id.charview_viewGear)
        viewXpReductions = findViewById(R.id.charview_viewXpReductions)
        viewAwards = findViewById(R.id.charview_viewAwards)
        convertPlanToCharacter = findViewById(R.id.charview_convertPlannedToNewCharacter)
        applyPlanToCharacter = findViewById(R.id.charview_applyPlanToCurrentCharacter)

        viewStats.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            DataManager.shared.setUpdateCallback(this::class) {
                buildView()
            }
            val intent = Intent(this, ViewCharacterStatsActivity::class.java)
            startActivity(intent)
        }

        viewSkillsTree.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            when (character.characterType()) {
                CharacterType.STANDARD, CharacterType.HIDDEN -> {
                    if (DataManager.shared.playerIsCurrentPlayer(character.playerId) && character.isAlive) {
                        val intent = Intent(this, PersonalNativeSkillTreeActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, OtherCharacterPersonalNativeSkillTreeActivity::class.java)
                        startActivity(intent)
                    }
                }
                CharacterType.NPC -> {
                    val intent = Intent(this, NPCPersonalNativeSkillTreeActivity::class.java)
                    startActivity(intent)
                }
                CharacterType.PLANNER -> {
                    val intent = if (DataManager.shared.playerIsCurrentPlayer(character.playerId)) {
                        Intent(this, PlannedCharacterPersonalNativeSkillTreeActivity::class.java)
                    } else {
                        Intent(this, OtherCharacterPersonalNativeSkillTreeActivity::class.java)
                    }
                    startActivity(intent)
                }
            }
        }

        viewSkillsList.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            if (character.characterType() == CharacterType.PLANNER && DataManager.shared.playerIsCurrentPlayer(character.playerId)) {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.ACTION, SkillsListActivity.SkillsListActivityActions.ALLOW_DELETE)
            }
            val intent = Intent(this, SkillsListActivity::class.java)
            startActivity(intent)
        }

        viewBio.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, ViewBioActivity::class.java)
            startActivity(intent)
        }

        viewGear.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, ViewGearActivity::class.java)
            startActivity(intent)
        }

        viewXpReductions.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, XpReductionsListActivity::class.java)
            startActivity(intent)
        }

        viewAwards.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.AWARDS_LIST, character.getAwardsSorted())
            val intent = Intent(this, ViewAwardsActivity::class.java)
            startActivity(intent)
        }

        convertPlanToCharacter.setOnClick {
            val player = DataManager.shared.getCurrentPlayer()!!
            if (player.getActiveCharacter() == null) {
                setAllLoadings(true)
                loadingLayout.isGone = false
                loadingLayout.setLoadingText("Determining Requirements...", showGettingContent = false)
                // Check to see if player meets the prereqs to make it
                val requirements = determineRequirementsToApplyPlan(null)
                val remainingRequirements = player.determineIfMeetsRequirements(requirements)
                if (remainingRequirements.allZero()) {
                    var spendMessage = "Spend"
                    if (requirements.xp > 0) {
                        spendMessage += "\n${remainingRequirements.xp} xp"
                    }
                    if (requirements.ft1s > 0) {
                        spendMessage += "\n${remainingRequirements.ft1s} Free Tier-1 Skills"
                    }
                    if (requirements.pp > 0) {
                        spendMessage += "\n${remainingRequirements.pp} Prestige Points"
                    }
                    spendMessage = "\non a new character?\nYou cannot undo this action."
                    AlertUtils.displayYesNoMessage(this@ViewCharacterActivity, "Are you sure?", spendMessage, onClickYes = { _, _ ->
                        // Go ahead and create character, all requirements met
                        // Don't allow any interactions while this is happening
                        loadingLayout.setLoadingText("Building Character...", showGettingContent = false)
                        enterCharNamePrompt()
                    }, onClickNo = { _, _ ->
                        setAllLoadings(false)
                        loadingLayout.isGone = true
                    })
                } else {
                    var message = "You are missing the following requirements:"
                    if (remainingRequirements.xp > 0) {
                        message += "\n${remainingRequirements.xp} xp"
                    }
                    if (remainingRequirements.ft1s > 0) {
                        message += "\n${remainingRequirements.ft1s} Free Tier-1 Skills"
                    }
                    if (remainingRequirements.pp > 0) {
                        message += "\n${remainingRequirements.pp} Prestige Points"
                    }
                    if (remainingRequirements.inf > 0) {
                        message += "\n${remainingRequirements.inf}% Infection (Note - without having a Character, you can never meet this requirement)"
                    }
                    AlertUtils.displayOkMessage(this@ViewCharacterActivity, "Cannot Create Character", message) { _, _ ->
                        setAllLoadings(false)
                        loadingLayout.isGone = true
                    }
                }
            }
        }

        applyPlanToCharacter.setOnClick {
            val player = DataManager.shared.getCurrentPlayer()!!
            setAllLoadings(true)
            loadingLayout.isGone = false
            loadingLayout.setLoadingText("Determining Requirements...", showGettingContent = false)
            // Check to see if player meets the prereqs to make it
            val requirements = determineRequirementsToApplyPlan(player.getActiveCharacter()!!)
            val remainingRequirements = player.determineIfMeetsRequirements(requirements)
            if (remainingRequirements.allZero()) {
                var spendMessage = "Spend"
                if (requirements.xp > 0) {
                    spendMessage += "\n${remainingRequirements.xp} xp"
                }
                if (requirements.ft1s > 0) {
                    spendMessage += "\n${remainingRequirements.ft1s} Free Tier-1 Skills"
                }
                if (requirements.pp > 0) {
                    spendMessage += "\n${remainingRequirements.pp} Prestige Points"
                }
                spendMessage = "\non a new character?\nYou cannot undo this action."
                AlertUtils.displayYesNoMessage(this@ViewCharacterActivity, "Are you sure?", spendMessage, onClickYes = { _, _ ->
                    // Go ahead and create character, all requirements met
                    // Don't allow any interactions while this is happening
                    loadingLayout.setLoadingText("Building Character...", showGettingContent = false)
                    enterCharNamePrompt()
                }, onClickNo = { _, _ ->
                    setAllLoadings(false)
                    loadingLayout.isGone = true
                })
            } else {
                var message = "You are missing the following requirements:"
                if (remainingRequirements.xp > 0) {
                    message += "\n${remainingRequirements.xp} xp"
                }
                if (remainingRequirements.ft1s > 0) {
                    message += "\n${remainingRequirements.ft1s} Free Tier-1 Skills"
                }
                if (remainingRequirements.pp > 0) {
                    message += "\n${remainingRequirements.pp} Prestige Points"
                }
                if (remainingRequirements.inf > 0) {
                    message += "\n${remainingRequirements.inf}% More Infection Than You Currently Have"
                }
                AlertUtils.displayOkMessage(this@ViewCharacterActivity, "Cannot Update You Character With The Planned Skills", message) { _, _ ->
                    setAllLoadings(false)
                    loadingLayout.isGone = true
                }
            }
        }
        buildView()
    }

    private fun enterCharNamePrompt() {
        AlertUtils.displayMessageWithInputs(this@ViewCharacterActivity,
            title = "Enter Character Name",
            messageInputs = listOf(MessageInput("name", sectionTitle = TextView(this@ViewCharacterActivity).apply { text = "You will need to create your backstory/bio later in the Account Tab" }, EditText(this@ViewCharacterActivity).apply { hint = "Full Character Name" }, null, null)),
            response = { messageOutput ->
                if (messageOutput.buttonPressed == ButtonTypePressed.POSITIVE) {
                    val validationResult = Validator.validate(messageOutput.getValuesForKey("name")?.editTextValue ?: "", ValidationType.FULL_NAME)
                    if (validationResult.hasError) {
                        AlertUtils.displayOkMessage(this@ViewCharacterActivity, "Validation Error(s)", validationResult.getErrorMessages()) { _, _ ->
                            enterCharNamePrompt()
                        }
                    } else {
                        createCharacter(messageOutput.getValuesForKey("name")?.editTextValue ?: "")
                    }
                } else {
                    loadingLayout.isGone = true
                    setAllLoadings(false)
                }
            }
        )
    }

    private fun createCharacter(charName: String) {
        val player = DataManager.shared.getCurrentPlayer()
        player!!.createCharacter(lifecycleScope, charName, "") { newCharacter ->
            if (newCharacter != null) {
                loadingLayout.setLoadingText("Base Character Created, Updating Data...", showGettingContent = false)
                DataManager.shared.load(lifecycleScope) {
                    DataManager.shared.getCharacter(newCharacter.id).ifLet({ existingChar ->
                        applyPlanToCharacter(existingChar)
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@ViewCharacterActivity)
                    })
                }
            } else {
                loadingLayout.isGone = true
                setAllLoadings(false)
            }
        }
    }

    private fun applyPlanToCharacter(existingCharacter: FullCharacterModel) {
        var existingChar = existingCharacter
        val plannerSkills = character.allPurchasedSkills().filter { it.baseXpCost() != 0 }
        val plannerSkillIds = plannerSkills.map { ps -> ps.id }
        val combinedSkills = existingChar.allNonPurchasedSkills().filter { it.id.equalsAnyOf(plannerSkillIds) }
        // Sort by order added to database (i.e. purchase order)
        val addedSkills: MutableList<String> = mutableListOf()
        plannerSkills.sortedBy { it.getCharSkillModel()?.id ?: -1 }.forEach { plnSkill ->
            val skill = combinedSkills.firstOrNull { it.id == plnSkill.id }
            if (skill != null) {
                loadingLayout.setLoadingText("Purchasing ${plnSkill.name}")
                val useFreeSkill = plnSkill.spentFt1s() > 0
                val charSkillCreateModel = CharacterSkillCreateModel(
                    existingChar.id,
                    skill.id,
                    useFreeSkill.ternary(0, skill.modXpCost()),
                    useFreeSkill.ternary(1, 0),
                    skill.prestigeCost())
                    existingChar.silentlyPurchaseSkill_NO_PROMPTS_EXCEPT_ERRORS(lifecycleScope, charSkillCreateModel) { success ->
                        if (success) {
                            addedSkills.add(plnSkill.name)
                            loadingLayout.setLoadingText("Updating Character...")
                            DataManager.shared.load(lifecycleScope) {
                                existingChar = DataManager.shared.getCharacter(existingChar.id)!!
                            }
                        }
                    }
            }
        }
        AlertUtils.displayOkMessage(this@ViewCharacterActivity, "Success!", "Added the following skills: \n${addedSkills.joinToString("\n") }\nto ${existingChar.fullName}!") { _, _ ->
            runOnUiThread {
                finish()
            }
        }
    }

    data class SkillRequirements(
        var xp: Int,
        var pp: Int,
        var ft1s: Int,
        var inf: Int
    ) {
        fun allZero(): Boolean {
            return  xp == 0 && pp == 0 && ft1s == 0 && inf == 0
        }
    }

    private fun determineRequirementsToApplyPlan(existingChar: FullCharacterModel?): SkillRequirements {
        val skillReqs = SkillRequirements(0, 0, 0, 0)
        val plannerSkills = character.allPurchasedSkills().filter { it.baseXpCost() != 0 }
        if (existingChar == null) {
            // Would need to create new char
            skillReqs.xp = plannerSkills.sumOf { it.spentXp() }
            skillReqs.pp = plannerSkills.sumOf { it.spentPp() }
            skillReqs.ft1s = plannerSkills.sumOf { it.spentFt1s() }
            skillReqs.inf = plannerSkills.maxOf { it.baseInfectionCost() }
        } else {
            // Existing char
            val plannerSkillIds = plannerSkills.map { ps -> ps.id }
            val combinedSkills = existingChar.allNonPurchasedSkills().filter { it.id.equalsAnyOf(plannerSkillIds) }
            var comRed = 0
            var profRed = 0
            var talRed = 0
            combinedSkills.forEach { combSkill ->
                val ps = plannerSkills.first { it.id == combSkill.id }
                if (combSkill.id.equalsAnyOf(listOf(Constants.SpecificSkillIds.combatAficionado_T, Constants.SpecificSkillIds.combatSpecialist_P, Constants.SpecificSkillIds.expertCombat))) {
                    comRed = -1
                    if (combSkill.id == Constants.SpecificSkillIds.combatAficionado_T) {
                        talRed = 1
                    }
                    if (combSkill.id == Constants.SpecificSkillIds.combatSpecialist_P) {
                        profRed = 1
                    }
                } else if (combSkill.id.equalsAnyOf(listOf(Constants.SpecificSkillIds.talentAficionado_C, Constants.SpecificSkillIds.talentSpecialist_P, Constants.SpecificSkillIds.expertTalent))) {
                    talRed = -1
                    if (combSkill.id == Constants.SpecificSkillIds.talentAficionado_C) {
                        comRed = 1
                    }
                    if (combSkill.id == Constants.SpecificSkillIds.talentSpecialist_P) {
                        profRed = 1
                    }
                } else if (combSkill.id.equalsAnyOf(listOf(Constants.SpecificSkillIds.professionAficionado_T, Constants.SpecificSkillIds.professionSpecialist_C, Constants.SpecificSkillIds.expertProfession))) {
                    profRed = -1
                    if (combSkill.id == Constants.SpecificSkillIds.professionAficionado_T) {
                        talRed = 1
                    }
                    if (combSkill.id == Constants.SpecificSkillIds.professionSpecialist_C) {
                        comRed = 1
                    }
                }
                if (ps.spentFt1s() > 0) {
                    skillReqs.ft1s += 1
                } else {
                    var xpVal = combSkill.modXpCost()
                    when (combSkill.skillTypeId) {
                        Constants.SkillTypes.talent -> {
                            xpVal = max(1, xpVal + talRed)
                        }
                        Constants.SkillTypes.combat -> {
                            xpVal = max(1, xpVal + comRed)
                        }
                        Constants.SkillTypes.profession -> {
                            xpVal = max(1, xpVal + profRed)
                        }
                    }
                    skillReqs.xp += xpVal
                }
                skillReqs.pp += combSkill.prestigeCost()
                if (combSkill.modInfectionCost() > 0) {
                    skillReqs.inf = max(skillReqs.inf, combSkill.modInfectionCost())
                }
            }
        }
        return skillReqs
    }

    private fun setAllLoadings(loading: Boolean) {
        viewStats.setLoading(loading)
        viewSkillsTree.setLoading(loading)
        viewSkillsList.setLoading(loading)
        viewBio.setLoading(loading)
        viewGear.setLoading(loading)
        viewXpReductions.setLoading(loading)
        viewAwards.setLoading(loading)
        convertPlanToCharacter.setLoading(loading)
        applyPlanToCharacter.setLoading(loading)
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "${character.fullName}\n(${character.getPostText()})")
        val playerChar = DataManager.shared.getCurrentPlayer()?.getActiveCharacter()
        if (playerChar != null) {
            applyPlanToCharacter.textView.text = "Apply Plan To: ${playerChar.fullName}"
        }

        when (character.characterType()) {
            CharacterType.STANDARD -> {
                playerName.isGone = false
                var showBio = character.approvedBio
                if (!showBio) {
                    showBio = DataManager.shared.playerIsCurrentPlayer(character.id)
                }
                viewBio.isGone = !showBio
                viewGear.isGone = false
                viewAwards.isGone = false
                viewXpReductions.isGone = false
                convertPlanToCharacter.isGone = true
                applyPlanToCharacter.isGone = true
            }
            CharacterType.PLANNER, CharacterType.NPC, CharacterType.HIDDEN -> {
                val isPlanned = character.characterType() == CharacterType.PLANNER
                val isOwnedByPlayer = DataManager.shared.playerIsCurrentPlayer(character.playerId)
                val playerHasCharacter = playerChar != null
                playerName.isGone = true
                viewBio.isGone = isPlanned
                viewGear.isGone = true
                viewAwards.isGone = true
                viewXpReductions.isGone = true
                convertPlanToCharacter.isGone = !(isPlanned && isOwnedByPlayer && !playerHasCharacter)
                applyPlanToCharacter.isGone = !(isPlanned && isOwnedByPlayer && playerHasCharacter)
            }
        }

        playerName.set(DataManager.shared.getPlayerForCharacter(character).fullName)
    }
}