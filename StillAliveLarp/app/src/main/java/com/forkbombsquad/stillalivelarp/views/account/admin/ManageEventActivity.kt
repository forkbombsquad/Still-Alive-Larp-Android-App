package com.forkbombsquad.stillalivelarp.views.account.admin

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.views.shared.CharactersListActivity
import com.forkbombsquad.stillalivelarp.views.shared.EventsListActivity
import com.forkbombsquad.stillalivelarp.views.shared.PlayersListActivity
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel

import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.ButtonTypePressed
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.MessageInput
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlue
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.globalRoll1to100
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

class ManageEventActivity : NoStatusBarActivity() {

    private lateinit var viewTitle: TextView
    private lateinit var title: KeyValueView
    private lateinit var date: KeyValueView
    private lateinit var startTime: KeyValueView
    private lateinit var endTime: KeyValueView
    private lateinit var isStarted: KeyValueView
    private lateinit var isFinished: KeyValueView
    private lateinit var description: KeyValueView
    private lateinit var edit: NavArrowButtonRed
    private lateinit var viewAttendees: NavArrowButtonBlue
    private lateinit var startFinishButton: LoadingButton

    private lateinit var event: FullEventModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_event)
        setupView()
    }

    private fun setupView() {
        event = DataManager.shared.getPassedData(listOf(EventsListActivity::class, MyAccountFragment::class), DataManagerPassedDataKey.SELECTED_EVENT)!!

        viewTitle = findViewById(R.id.manageevent_viewtitle)
        title = findViewById(R.id.manageevent_title)
        date = findViewById(R.id.manageevent_date)
        startTime = findViewById(R.id.manageevent_startTime)
        endTime = findViewById(R.id.manageevent_endTime)
        isStarted = findViewById(R.id.manageevent_isStarted)
        isFinished = findViewById(R.id.manageevent_isFinished)
        description = findViewById(R.id.manageevent_description)
        edit = findViewById(R.id.manageevent_edit)
        viewAttendees = findViewById(R.id.manageevent_viewAttendees)
        startFinishButton = findViewById(R.id.manageevent_startFinishButton)

        edit.setOnClick {
            DataManager.shared.addActivityToClose(this, false)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_EVENT, event)
            val intent = Intent(this, EditEventActivity::class.java)
            startActivity(intent)
        }

        viewAttendees.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_EVENT, event)
            val intent = Intent(this, ViewEventAttendeesActivity::class.java)
            startActivity(intent)
        }

        startFinishButton.setOnClick {
            startFinishButton.setLoading(true)
            var starting = false
            if (!event.isStarted) {
                event.isStarted = true
                starting = true
            } else {
                event.isFinished = true
            }
            val updateEventRequest = AdminService.UpdateEvent()
            lifecycleScope.launch {
                updateEventRequest.successfulResponse(UpdateModelSP(event)).ifLet({ _ ->
                    if (starting) {
                        // Event is being started for first time - simple success
                        val davis = DataManager.shared.getAllCharacters().first { it.id == Constants.SpecificCharacterIds.commanderDavis }
                        AlertUtils.displayOkMessage(
                            this@ManageEventActivity,
                            "Materials For Sale!",
                            "Materials Gathered By NPCs over the past month that are for sale in the camp store:\n\nWood: ${davis.woodSupplies}\nMetal: ${davis.metalSupplies}\nCloth: ${davis.clothSupplies}\nTech: ${davis.techSupplies}\nMedical: ${davis.medicalSupplies}"
                        ) { _, _ ->
                            AlertUtils.displaySuccessMessage(this@ManageEventActivity, "Event Started!") { _, _ ->
                                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                                DataManager.shared.closeActiviesToClose()
                                finish()
                            }
                        }
                    } else {
                        // Event is being finished - show pre-finish dialog chain
                        promptForRaffleAward()
                    }
                }, {
                    startFinishButton.setLoading(false)
                })
            }
        }

        buildView()
    }

    private fun promptForRaffleAward() {
        AlertUtils.displayMessage(
            this,
            "Award Raffle Winner?",
            "Players: Xp, Free Tier 1 Skils, Prestige Points\nCharacters: Infection, Ammo, Materials",
            arrayOf(
                AlertButton("Character", { _, _ ->
                    launchCharacterAwardSelection()
                }, ButtonType.POSITIVE),
                AlertButton("Continue", { _, _ ->
                    finishEventFlowPromptForFood()
                }, ButtonType.NEUTRAL),
                AlertButton("Player", { _, _ ->
                    launchPlayerAwardSelection()
                }, ButtonType.NEGATIVE),
            )
        )
    }

    private fun launchCharacterAwardSelection() {
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD))
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, AwardCharacterActivity::class)
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character to Award")
        DataManager.shared.setUpdateCallback(this::class) {
            // After award is given, loop back to raffle prompt
            this@ManageEventActivity.promptForRaffleAward()
        }
        val intent = Intent(this, CharactersListActivity::class.java)
        startActivity(intent)
    }

    private fun launchPlayerAwardSelection() {
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.PLAYER_LIST, DataManager.shared.players)
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, AwardPlayerActivity::class)
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Player To Award")
        DataManager.shared.setUpdateCallback(this::class) {
            // After award is given, loop back to raffle prompt
            this@ManageEventActivity.promptForRaffleAward()
        }
        val intent = Intent(this, PlayersListActivity::class.java)
        startActivity(intent)
    }

    private fun finishEventFlowPromptForFood() {
        AlertUtils.displayMessageWithInputs(
            this,
            "How Much Food Was Collected?",
                listOf(
                    MessageInput(
                    "req",
                        TextView(this).apply {
                            text = "Food Required To Meet Threshold (per player attending this event):"
                        },
                        EditText(this).apply {
                            hint = "Per player, default is 2"
                            setText("2")
                            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                        },
                    null,
                    null
                    ),
                    MessageInput(
                        "food",
                        TextView(this).apply {
                            text = "Amount Of Food Collected:"
                        },
                        EditText(this).apply {
                            hint = "Food Collected"
                            inputType = InputType.TYPE_CLASS_NUMBER
                        },
                        null,
                        null
                    )
                ),
                "Submit",
                "Skip") { messageOutput ->
            if (messageOutput.buttonPressed == ButtonTypePressed.POSITIVE) {
                val req = (messageOutput.getValuesForKey("req")?.editTextValue ?: "0.0").toDoubleOrNull()
                val food = (messageOutput.getValuesForKey("food")?.editTextValue ?: "0.0").toIntOrNull()

                if (req == null || food == null) {
                    AlertUtils.displayOkMessage(this, "Must enter a number!", "Please") { _, _ ->
                        finishEventFlowPromptForFood()
                    }
                } else {

                    val attendees = event.attendees.count().toDouble()
                    val totalFoodRequired = ceil(attendees * req).toInt()
                    val percentagePerFood = (100.0 / totalFoodRequired.toDouble())
                    val allNpcs = DataManager.shared.getAllCharacters(type = CharacterType.NPC).filter { it.isAlive }

                    if (food == totalFoodRequired) { // THRESHOLD
                        // No Benefits or Downsides
                        AlertUtils.displayOkMessage(
                            this,
                            "Food Threshold Reached!",
                            "No additional bonuses or penalties!\n\nFood Donated: $food\nFood Required: $totalFoodRequired")
                        { _, _ ->
                            val cm = DataManager.shared.getAllCharacters().first { it.id == Constants.SpecificCharacterIds.commanderDavis }
                            val editedChar = CharacterModel(
                                id = cm.id,
                                fullName = cm.fullName,
                                startDate = cm.startDate,
                                isAlive = cm.isAlive.toString().uppercase(),
                                deathDate = cm.deathDate,
                                infection = cm.infection,
                                bio = cm.bio,
                                approvedBio = cm.approvedBio.toString().uppercase(),
                                bullets = cm.bullets.toString(),
                                megas = cm.megas.toString(),
                                rivals = cm.rivals.toString(),
                                rockets = cm.rockets.toString(),
                                bulletCasings = cm.bulletCasings.toString(),
                                clothSupplies = "0",
                                woodSupplies = "0",
                                metalSupplies = "0",
                                techSupplies = "0",
                                medicalSupplies = "0",
                                armor = cm.armor,
                                unshakableResolveUses = cm.unshakableResolveUses.toString(),
                                mysteriousStrangerUses = cm.mysteriousStrangerUses.toString(),
                                playerId = cm.playerId,
                                characterTypeId = cm.characterTypeId
                            )
                            startFinishButton.setLoadingWithText("Updating Commander With Zeroed Out Materials...")
                            val updateCharRequest = AdminService.UpdateCharacter()
                            lifecycleScope.launch {
                                updateCharRequest.successfulResponse(UpdateModelSP(editedChar)).ifLet({ _ ->
                                    startFinishButton.setLoading(true)
                                    finishEventFlow()
                                }, {
                                    startFinishButton.setLoading(true)
                                    AlertUtils.displaySomethingWentWrong(this@ManageEventActivity)
                                    finishEventFlow()
                                })
                            }
                        }
                    } else if (food > totalFoodRequired) { // ABOVE THRESHOLD
                        val npcGateredMats = getNPCGatheredMaterials(allNpcs)
                        AlertUtils.displayOkMessage(
                            this,
                            "Food Threshold Exceeded!",
                            npcGateredMats.getPrintString()
                        ) { _, _ ->
                            val cm = DataManager.shared.getAllCharacters().first { it.id == Constants.SpecificCharacterIds.commanderDavis }
                            val editedChar = CharacterModel(
                                id = cm.id,
                                fullName = cm.fullName,
                                startDate = cm.startDate,
                                isAlive = cm.isAlive.toString().uppercase(),
                                deathDate = cm.deathDate,
                                infection = cm.infection,
                                bio = cm.bio,
                                approvedBio = cm.approvedBio.toString().uppercase(),
                                bullets = cm.bullets.toString(),
                                megas = cm.megas.toString(),
                                rivals = cm.rivals.toString(),
                                rockets = cm.rockets.toString(),
                                bulletCasings = cm.bulletCasings.toString(),
                                clothSupplies = npcGateredMats.cloth.toString(),
                                woodSupplies = npcGateredMats.wood.toString(),
                                metalSupplies = npcGateredMats.metal.toString(),
                                techSupplies = npcGateredMats.tech.toString(),
                                medicalSupplies = npcGateredMats.medical.toString(),
                                armor = cm.armor,
                                unshakableResolveUses = cm.unshakableResolveUses.toString(),
                                mysteriousStrangerUses = cm.mysteriousStrangerUses.toString(),
                                playerId = cm.playerId,
                                characterTypeId = cm.characterTypeId
                            )
                            startFinishButton.setLoadingWithText("Updating Commander With New Materials...")
                            val updateCharRequest = AdminService.UpdateCharacter()
                            lifecycleScope.launch {
                                updateCharRequest.successfulResponse(UpdateModelSP(editedChar)).ifLet({ _ ->
                                    startFinishButton.setLoading(true)
                                    finishEventFlowNPCAttraction(allNpcs, food, totalFoodRequired, percentagePerFood)
                                }, {
                                    startFinishButton.setLoading(true)
                                    AlertUtils.displaySomethingWentWrong(this@ManageEventActivity)
                                    finishEventFlowNPCAttraction(allNpcs, food, totalFoodRequired, percentagePerFood)
                                })
                            }
                        }
                    } else { // BELOW THRESHOLD
                        val cm = DataManager.shared.getAllCharacters().first { it.id == Constants.SpecificCharacterIds.commanderDavis }
                        val editedChar = CharacterModel(
                            id = cm.id,
                            fullName = cm.fullName,
                            startDate = cm.startDate,
                            isAlive = cm.isAlive.toString().uppercase(),
                            deathDate = cm.deathDate,
                            infection = cm.infection,
                            bio = cm.bio,
                            approvedBio = cm.approvedBio.toString().uppercase(),
                            bullets = cm.bullets.toString(),
                            megas = cm.megas.toString(),
                            rivals = cm.rivals.toString(),
                            rockets = cm.rockets.toString(),
                            bulletCasings = cm.bulletCasings.toString(),
                            clothSupplies = "0",
                            woodSupplies = "0",
                            metalSupplies = "0",
                            techSupplies = "0",
                            medicalSupplies = "0",
                            armor = cm.armor,
                            unshakableResolveUses = cm.unshakableResolveUses.toString(),
                            mysteriousStrangerUses = cm.mysteriousStrangerUses.toString(),
                            playerId = cm.playerId,
                            characterTypeId = cm.characterTypeId
                        )
                        startFinishButton.setLoadingWithText("Updating Commander With Zeroed Out Materials...")
                        val updateCharRequest = AdminService.UpdateCharacter()
                        lifecycleScope.launch {
                            updateCharRequest.successfulResponse(UpdateModelSP(editedChar)).ifLet({ _ ->
                                startFinishButton.setLoading(true)
                                finishEventFlowDeathMaybe(allNpcs, totalFoodRequired, food, percentagePerFood)
                            }, {
                                startFinishButton.setLoading(true)
                                AlertUtils.displaySomethingWentWrong(this@ManageEventActivity)
                                finishEventFlowDeathMaybe(allNpcs, totalFoodRequired, food, percentagePerFood)
                            })
                        }
                    }
                }
            } else {
                finishEventFlow()
            }
        }
    }

    private fun finishEventFlowDeathMaybe(allNpcs: List<FullCharacterModel>, totalFoodRequired: Int, food: Int, percentagePerFood: Double) {
        val chanceOfDeath = ceil((totalFoodRequired - food).toDouble() * percentagePerFood).toInt()
        var rollsMessage = ""
        var deadNPC: FullCharacterModel? = null
        for (npc in allNpcs.shuffled()) {
            val roll = globalRoll1to100()
            if (roll <= chanceOfDeath) {
                rollsMessage += "${npc.fullName} DIED OF STARVATION! ($roll% ≤ $chanceOfDeath%)"
                deadNPC = npc
            } else {
                rollsMessage += "${npc.fullName} Survived ($roll% > $chanceOfDeath%)"
            }
            if (deadNPC != null) {
                break
            } else {
                rollsMessage += "\n"
            }
        }

        AlertUtils.displayOkMessage(
            this,
            (deadNPC != null).ternary("STARVATION!", "Everyone Survived!"),
            rollsMessage
        ) { _, _ ->
            if (deadNPC != null) {
                startFinishButton.setLoadingWithText("Killing ${deadNPC.fullName}...")
                val cm = deadNPC
                val editedNPC = CharacterModel(
                    id = cm.id,
                    fullName = cm.fullName,
                    startDate = cm.startDate,
                    isAlive = "FALSE",
                    deathDate = LocalDate.now().yyyyMMddFormatted(),
                    infection = cm.infection,
                    bio = cm.bio,
                    approvedBio = cm.approvedBio.toString().uppercase(),
                    bullets = cm.bullets.toString(),
                    megas = cm.megas.toString(),
                    rivals = cm.rivals.toString(),
                    rockets = cm.rockets.toString(),
                    bulletCasings = cm.bulletCasings.toString(),
                    clothSupplies = cm.clothSupplies.toString(),
                    woodSupplies = cm.woodSupplies.toString(),
                    metalSupplies = cm.metalSupplies.toString(),
                    techSupplies = cm.techSupplies.toString(),
                    medicalSupplies = cm.medicalSupplies.toString(),
                    armor = cm.armor,
                    unshakableResolveUses = cm.unshakableResolveUses.toString(),
                    mysteriousStrangerUses = cm.mysteriousStrangerUses.toString(),
                    playerId = cm.playerId,
                    characterTypeId = cm.characterTypeId
                )
                val updateCharRequest = AdminService.UpdateCharacter()
                lifecycleScope.launch {
                    updateCharRequest.successfulResponse(UpdateModelSP(editedNPC)).ifLet({ _ ->
                        startFinishButton.setLoading(true)
                        finishEventFlow()
                    }, {
                        startFinishButton.setLoading(true)
                        AlertUtils.displaySomethingWentWrong(this@ManageEventActivity)
                        finishEventFlow()
                    })
                }
            } else {
                finishEventFlow()
            }
        }
    }
    private fun getNPCGatheredMaterials(npcs: List<FullCharacterModel>): NPCGatheredMaterials {
        val ngm = NPCGatheredMaterials()
        npcs.forEach { _ ->
            ngm.addNew(globalRoll1to100())
        }
        return ngm
    }

    private data class NPCGatheredMaterials(var wood: Int = 0, var metal: Int = 0, var cloth: Int = 0, var tech: Int = 0, var medical: Int = 0) {
        fun getPrintString(): String {
            return "Your NPCs were well fed enough to gather additional materials for sale at the next event!\n\nWood: $wood\nMetal: $metal\nCloth: $cloth\nTech: $tech\nMedical $medical"
        }

        fun addNew(rollBetween1and100: Int) {
            when (rollBetween1and100) {
                in 1..20 -> wood += 1
                in 21..40 -> metal += 1
                in 41..60 -> cloth += 1
                in 61..80 -> tech += 1
                in 81..100 -> medical += 1
            }
        }
    }
    private fun finishEventFlowNPCAttraction(allNpcs: List<FullCharacterModel>, food: Int, totalFoodRequired: Int, percentagePerFood: Double) {
        // Material Awards and Chance For New NPC (if Room)
        val maxNpcs = DataManager.shared.campStatus?.npcSlots ?: 10
        if (allNpcs.count() >= maxNpcs) {
            // NPC Slots are Full
            AlertUtils.displayOkMessage(
                this,
                "NPC Slots Full!",
                "You cannot attract any more NPCs without increasing the NPC capacity"
            ) { _, _ ->
                finishEventFlow()
            }
        } else {
            // Chance For New NPCs is equal to half the percentage that food was worth below threshold. Capped at 50%.
            val chanceForNPCAtrraction = 100 - min(ceil((food - totalFoodRequired).toDouble() * (percentagePerFood / 2.0)).toInt(), 50)
            val roll = globalRoll1to100()
            if (roll > chanceForNPCAtrraction) {
                AlertUtils.displayOkMessage(
                    this,
                    "Success! New NPC Attracted!",
                    "Soon, an opportunity to recruit a new NPC will arise!\n(Rolled: $roll%, which is > than $chanceForNPCAtrraction%)"
                ) { _, _ ->
                    finishEventFlow()
                }
            } else {
                AlertUtils.displayOkMessage(
                    this,
                    ":( Failed To Attract A New NPC This Time",
                    "Rolled $roll%, which was ≤ $chanceForNPCAtrraction%!"
                ) { _, _ ->
                    finishEventFlow()
                }
            }
        }
    }

    private fun finishEventFlow() {
        AlertUtils.displaySuccessMessage(this, "${event.title} Finished!") { _, _ ->
            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
            DataManager.shared.closeActiviesToClose()
            finish()
        }
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(viewTitle, "Manage Event")
        title.set(event.title)
        date.set(event.date.yyyyMMddToMonthDayYear())
        startTime.set(event.startTime)
        endTime.set(event.endTime)
        isStarted.set(event.isStarted.toString())
        isFinished.set(event.isFinished.toString())
        description.set(event.description)

        if (!event.isFinished) {
            startFinishButton.isGone = false
            startFinishButton.set(event.isStarted.ternary("Finish Event", "Start Event"))
        } else {
            startFinishButton.isGone = true
        }

        if (DataManager.shared.offlineMode) {
            startFinishButton.isGone = true
            edit.isGone = true
        }
    }
}