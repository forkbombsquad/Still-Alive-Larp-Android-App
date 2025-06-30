package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.CheckInOutBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.CaptureActivityPortrait
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.KeyValuePickerView
import com.forkbombsquad.stillalivelarp.utils.KeyValueTextFieldView
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.min

class CheckOutPlayerActivity : NoStatusBarActivity() {

    private lateinit var playerName: KeyValueView
    private lateinit var totalEventsAttended: KeyValueView
    private lateinit var totalNpcEventsAttended: KeyValueView
    private lateinit var lastEventAttended: KeyValueView

    private lateinit var characterName: KeyValueView

    private lateinit var characterLayout: LinearLayout
    private lateinit var infection: KeyValueTextFieldView
    private lateinit var reduceInfection: KeyValueView
    private lateinit var bullets: KeyValueTextFieldView
    private lateinit var megas: KeyValueTextFieldView
    private lateinit var rivals: KeyValueTextFieldView
    private lateinit var rockets: KeyValueTextFieldView
    private lateinit var casings: KeyValueTextFieldView
    private lateinit var cloth: KeyValueTextFieldView
    private lateinit var wood: KeyValueTextFieldView
    private lateinit var metal: KeyValueTextFieldView
    private lateinit var tech: KeyValueTextFieldView
    private lateinit var medical: KeyValueTextFieldView
    private lateinit var mysteriousStranger: KeyValueTextFieldView
    private lateinit var unshakableResolve: KeyValueTextFieldView
    private lateinit var armor: KeyValuePickerView
    private lateinit var isAlive: KeyValuePickerView

    private lateinit var checkoutButton: LoadingButton

    private lateinit var barcodeModel: CheckInOutBarcodeModel
    private lateinit var player: FullPlayerModel
    private var character: FullCharacterModel? = null
    private var isNpc = false
    private lateinit var event: FullEventModel
    private lateinit var eventAttendeeModel: EventAttendeeModel

    private val barcodeScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            globalFromJson<CheckInOutBarcodeModel>(result.contents).ifLet({
                barcodeModel = it
                recalculateModels()
                buildView()
            }, {
                AlertUtils.displayError(this, "Unable to parse barcode data!") { _, _ ->
                    finish()
                }
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_player)
        setupView()
    }

    private fun recalculateModels() {
        player = DataManager.shared.players.first { it.id == barcodeModel.playerId }
        barcodeModel.characterId.ifLet({ charId ->
            this.character = DataManager.shared.getCharacter(charId)
            isNpc = false
        }, {
            this.character = null
            isNpc = true
        })
        event = DataManager.shared.events.first { it.id == barcodeModel.eventId }
        eventAttendeeModel = event.attendees.first { it.playerId == barcodeModel.playerId }
    }

    private fun setupView() {
        barcodeModel = DataManager.shared.getPassedData(AdminPanelActivity::class, DataManagerPassedDataKey.BARCODE)!!
        recalculateModels()

        playerName = findViewById(R.id.checkoutplayer_playerName)
        totalEventsAttended = findViewById(R.id.checkoutplayer_totalEvents)
        totalNpcEventsAttended = findViewById(R.id.checkoutplayer_totalNpcEvents)
        lastEventAttended = findViewById(R.id.checkoutplayer_lastEvent)
        characterName = findViewById(R.id.checkoutplayer_characterName)
        characterLayout = findViewById(R.id.checkoutplayer_characterLayout)
        infection = findViewById(R.id.checkoutplayer_infection)
        reduceInfection = findViewById(R.id.checkoutplayer_reduceInfection)
        bullets = findViewById(R.id.checkoutplayer_bullets)
        megas = findViewById(R.id.checkoutplayer_megas)
        rivals = findViewById(R.id.checkoutplayer_rivals)
        rockets = findViewById(R.id.checkoutplayer_rockets)
        casings = findViewById(R.id.checkoutplayer_casings)
        cloth = findViewById(R.id.checkoutplayer_cloth)
        wood = findViewById(R.id.checkoutplayer_wood)
        metal = findViewById(R.id.checkoutplayer_metal)
        tech = findViewById(R.id.checkoutplayer_tech)
        medical = findViewById(R.id.checkoutplayer_medical)
        mysteriousStranger = findViewById(R.id.checkoutplayer_mysteriousStranger)
        unshakableResolve = findViewById(R.id.checkoutplayer_unshakableResolve)
        armor = findViewById(R.id.checkoutplayer_armor)
        isAlive = findViewById(R.id.checkoutplayer_alive)
        checkoutButton = findViewById(R.id.checkoutplayer_checkOutButton)

        val armorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("None", "Metal", "Bullet Proof"))
        armor.valuePickerView.adapter = armorAdapter
        armor.valuePickerView.setSelection(0)

        val aliveAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Alive", "Dead"))
        isAlive.valuePickerView.adapter = aliveAdapter
        isAlive.valuePickerView.setSelection(0)

        checkoutButton.setOnClick {
            val valResult = validateFields()
            if (!valResult.hasError) {
                character.ifLet({ char ->
                    if (isPassedThreshold() && isAlive.valuePickerView.selectedItemPosition == 0) {
                        AlertUtils.displayMessage(
                            context = this,
                            title = "Warning!",
                            message = "${char.fullName} has passed an infection threshold! Make sure to make the check to see if they turn into a zombie!\n\n${getThresholdCheckSkillsList(char.getRelevantBarcodeSkills())}",
                            buttons = arrayOf(
                                AlertButton(
                                    text = "Check Passed!",
                                    onClick = { _, _ ->
                                        checkoutStepOne()
                                    },
                                    buttonType = ButtonType.POSITIVE
                                ),
                                AlertButton(
                                    text = "Go Back",
                                    onClick = { _, _ -> },
                                    buttonType = ButtonType.NEUTRAL
                                )
                            )
                        )
                    } else {
                        checkoutStepOne()
                    }
                }, {
                    checkoutStepOne()
                })
            } else {
                AlertUtils.displayValidationError(this, valResult.getErrorMessages())
            }
        }
        buildView()
    }

    private fun checkoutStepOne() {
        if (!isNpc && isAlive.valuePickerView.selectedItemPosition == 1) {
            AlertUtils.displayMessage(
                context = this,
                title = "Warning!",
                message = "${character?.fullName ?: ""} has seemingly perished, but they still have a chance! Make sure you roll 1d10 to see if they miraculously survive (by rolling a 10)!\n\n${getDeathCheckSkillsList(character?.getRelevantBarcodeSkills() ?: listOf())}",
                buttons = arrayOf(
                    AlertButton(
                        text = "Still Dead!",
                        onClick = { _, _ ->
                            checkoutStepTwo()
                        },
                        buttonType = ButtonType.POSITIVE
                    ),
                    AlertButton(
                        text = "They Survived! Go Back",
                        onClick = { _, _ -> },
                        buttonType = ButtonType.NEUTRAL
                    )
                )
            )
        } else {
            checkoutStepTwo()
        }
    }

    private fun checkoutStepTwo() {
        character.ifLet({ cm ->
            val editedChar = CharacterModel(
                id = cm.id,
                fullName = cm.fullName,
                startDate = cm.startDate,
                isAlive = (isAlive.valuePickerView.selectedItemPosition == 0).ternary("TRUE", "FALSE"),
                deathDate = (isAlive.valuePickerView.selectedItemPosition == 0).ternary("", LocalDate.now().yyyyMMddFormatted()),
                infection = infection.getValue(),
                bio = cm.bio,
                approvedBio = cm.approvedBio.toString().uppercase(),
                bullets = bullets.getValue(),
                megas = megas.getValue(),
                rivals = rivals.getValue(),
                rockets = rockets.getValue(),
                bulletCasings = casings.getValue(),
                clothSupplies = cloth.getValue(),
                woodSupplies = wood.getValue(),
                metalSupplies = metal.getValue(),
                techSupplies = tech.getValue(),
                medicalSupplies = medical.getValue(),
                armor = armor.valuePickerView.selectedItem as String,
                unshakableResolveUses = unshakableResolve.getValue(),
                mysteriousStrangerUses = mysteriousStranger.getValue(),
                playerId = cm.playerId,
                characterTypeId = cm.characterTypeId
            )
            checkoutButton.setLoadingWithText("Updating Character")
            val updateCharRequest = AdminService.UpdateCharacter()
            lifecycleScope.launch {
                updateCharRequest.successfulResponse(UpdateModelSP(editedChar)).ifLet({ _ ->
                    checkoutStepThree()
                }, {
                    checkoutButton.setLoading(false)
                    restartScanner()
                })
            }
        }, {
            checkoutStepThree()
        })
    }

    private fun checkoutStepThree() {
        var needToAwardExtraXp = false
        if (!isNpc && isAlive.valuePickerView.selectedItemPosition == 1) {
            // Character is dead, award extra xp
            needToAwardExtraXp = true
        }
        val xpAmount = isNpc.ternary(2, 1)

        val xp = player.experience + xpAmount
        val events = player.numEventsAttended + 1
        val npcEvents = player.numNpcEventsAttended + isNpc.ternary(1, 0)

        val playerUpdate = PlayerModel(
            id = player.id,
            username = player.username,
            fullName =  player.fullName,
            startDate = player.startDate,
            experience = xp.toString(),
            freeTier1Skills = player.freeTier1Skills.toString(),
            prestigePoints = player.prestigePoints.toString(),
            isCheckedIn = "FALSE",
            isCheckedInAsNpc = "FALSE",
            lastCheckIn = LocalDate.now().yyyyMMddFormatted(),
            numEventsAttended = events.toString(),
            numNpcEventsAttended = npcEvents.toString(),
            isAdmin = player.isAdmin.toString().uppercase()
        )

        checkoutButton.setLoadingWithText("Updating Player")

        val playerUpdateRequest = AdminService.UpdatePlayer()
        lifecycleScope.launch {
            playerUpdateRequest.successfulResponse(UpdateModelSP(playerUpdate)).ifLet({_ ->
                checkoutStepFour(needToAwardExtraXp)
            }, {
                checkoutButton.setLoading(false)
                restartScanner()
            })
        }
    }

    private fun checkoutStepFour(needToAwardExtraXp: Boolean) {
        checkoutButton.setLoadingWithText("Updating Records")

        val eventAttendeeUpdate = EventAttendeeModel(
            id = eventAttendeeModel.id,
            playerId = eventAttendeeModel.playerId,
            characterId = eventAttendeeModel.characterId,
            eventId = eventAttendeeModel.eventId,
            isCheckedIn = "FALSE",
            asNpc = isNpc.ternary("TRUE", "FALSE")
        )

        val updateAttendeeRequest = AdminService.UpdateEventAttendee()
        lifecycleScope.launch {
            updateAttendeeRequest.successfulResponse(UpdateModelSP(eventAttendeeUpdate)).ifLet({ eventAttendee ->
                if (needToAwardExtraXp && !isNpc && character != null) {
                    checkoutButton.setLoadingWithText("Calculating Death Xp Bonus")
                    val spentXp = character!!.getAllXpSpent()
                    val spentPp = character!!.getAllSpentPrestigePoints()
                    var adjustedXp = spentXp / 2

                    var max = player.numEventsAttended
                    max += player.numNpcEventsAttended // Adding double xp for npc events

                    adjustedXp = min(max, adjustedXp)

                    checkoutButton.setLoadingWithText("Refunding Xp")

                    val award = AwardCreateModel.createPlayerAward(
                        playerId = player.id,
                        awardType = AwardPlayerType.XP,
                        reason = "Death of Character: ${character!!.fullName}",
                        amount = adjustedXp.toString()
                    )
                    val createAwardRequest = AdminService.AwardPlayer()
                    lifecycleScope.launch {
                        createAwardRequest.successfulResponse(AwardCreateSP(award)).ifLet({ _ ->
                            if (spentPp > 0) {
                                checkoutButton.setLoadingWithText("Refunding Prestige Points")

                                val a = AwardCreateModel.createPlayerAward(
                                    playerId = player.id,
                                    awardType = AwardPlayerType.PRESTIGEPOINTS,
                                    reason = "Death of Character: ${character!!.fullName}",
                                    amount = spentPp.toString()
                                )
                                val car = AdminService.AwardPlayer()
                                lifecycleScope.launch {
                                    car.successfulResponse(AwardCreateSP(a)).ifLet({ _ ->
                                        checkoutButton.setLoading(false)
                                        showSuccessAlertAllowingRescan("Successfully Checked Out ${player.fullName}!")
                                    }, {
                                        checkoutButton.setLoading(false)
                                        showSuccessAlertAllowingRescan("Successfully Checked Out ${player.fullName}!\nBut unable to award death pp!")
                                    })
                                }
                            } else {
                                checkoutButton.setLoading(false)
                                showSuccessAlertAllowingRescan("Successfully Checked Out ${player.fullName}!")
                            }
                        }, {
                            checkoutButton.setLoading(false)
                            showSuccessAlertAllowingRescan("Successfully Checked Out ${player.fullName}!\nBut unable to award death xp!")
                        })
                    }
                } else {
                    checkoutButton.setLoading(false)
                    showSuccessAlertAllowingRescan("Successfully Checked Out ${player.fullName}!")
                }
            }, {
                checkoutButton.setLoading(false)
                restartScanner()
            })
        }
    }

    private fun showSuccessAlertAllowingRescan(message: String) {
        AlertUtils.displayMessage(
            context = this,
            title = "Success",
            message = message,
            buttons = arrayOf(
                AlertButton("Scan Another", { _, _ ->
                    restartScanner()
                }, ButtonType.POSITIVE),
                AlertButton("Finished", { _, _ ->
                    DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                    finish()
                }, ButtonType.NEGATIVE),
            )
        )
    }

    private fun restartScanner() {
        val sc = ScanOptions()
        sc.setOrientationLocked(true)
        sc.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        sc.captureActivity = CaptureActivityPortrait::class.java
        barcodeScanner.launch(sc)
    }

    private fun buildView() {
        playerName.set(player.fullName)
        totalEventsAttended.set("${player.numEventsAttended}+1")
        totalNpcEventsAttended.set(isNpc.ternary("${player.numNpcEventsAttended}+1", player.numNpcEventsAttended.toString()))
        lastEventAttended.set(LocalDate.now().yyyyMMddFormatted().yyyyMMddToMonthDayYear())

        // Character Section
        character.ifLet({ char ->
            characterLayout.isGone = false
            characterName.set(char.fullName)
            infection.set(char.infection)
            reduceInfection.isGone = true
            infection.div.isGone = false

            val relevantSkills = char.getRelevantBarcodeSkills()

            if (hasRegressionOrRemission(relevantSkills)) {
                infection.div.isGone = true
                reduceInfection.isGone = false
                reduceInfection.set(getReductionAmount(relevantSkills))
            }

            val mysterStrangerTotal = mysteriousStrangerTotal(relevantSkills)
            mysteriousStranger.isGone = mysterStrangerTotal == 0
            if (mysterStrangerTotal > 0) {
                mysteriousStranger.set("Mysterious Stranger Uses (out of $mysterStrangerTotal)", char.mysteriousStrangerUses.toString())
            }
            val hasUnshakableResolve = hasUnshakableResolve(relevantSkills)
            unshakableResolve.isGone = !hasUnshakableResolve
            if (hasUnshakableResolve) {
                unshakableResolve.set(char.unshakableResolveUses.toString())
            }

            bullets.set(char.bullets.toString())
            megas.set(char.megas.toString())
            rivals.set(char.rivals.toString())
            rockets.set(char.rockets.toString())
            casings.set(char.bulletCasings.toString())
            cloth.set(char.clothSupplies.toString())
            wood.set(char.woodSupplies.toString())
            metal.set(char.metalSupplies.toString())
            tech.set(char.techSupplies.toString())
            medical.set(char.medicalSupplies.toString())

        }, {
            characterLayout.isGone = true
            characterName.set("NPC")
        })
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(
            ValidationGroup(infection, ValidationType.INFECTION),
            ValidationGroup(bullets, ValidationType.BULLETS),
            ValidationGroup(megas, ValidationType.MEGAS),
            ValidationGroup(rivals, ValidationType.RIVALS),
            ValidationGroup(rockets, ValidationType.ROCKETS),
            ValidationGroup(casings, ValidationType.BULLET_CASINGS),
            ValidationGroup(cloth, ValidationType.CLOTH),
            ValidationGroup(wood, ValidationType.WOOD),
            ValidationGroup(metal, ValidationType.METAL),
            ValidationGroup(tech, ValidationType.TECH),
            ValidationGroup(medical, ValidationType.MEDICAL)
        ))
    }

    private fun getDeathCheckSkillsList(relevantSkills: List<FullCharacterModifiedSkillModel>): String {
        val skl = Constants.SpecificSkillIds
        var skills = ""
        var gamblerSkillsCount = 0
        for (skill in relevantSkills) {
            if (skill.id.equalsAnyOf(skl.gamblerTypeSkills)) {
                gamblerSkillsCount++
            }
        }
        if (gamblerSkillsCount > 0) {
            skills = "\n$gamblerSkillsCount level(s) of gambler skills, allowing them to reroll dice or reflip coins and take the best result - once for each level of the skill."
        }
        if (skills.isNotEmpty()) {
            skills = "Relevant Skills:$skills"
        }
        return skills
    }

    private fun getThresholdCheckSkillsList(relevantSkills: List<FullCharacterModifiedSkillModel>): String {
        val skl = Constants.SpecificSkillIds
        var skills = ""
        var gamblerSkillsCount = 0
        for (skill in relevantSkills) {
            if (skill.id.equalsAnyOf(skl.gamblerTypeSkills)) {
                gamblerSkillsCount++
            }
            if (skill.id == skl.willToLive) {
                skills += "\nWill To Live skill - They may flip a coin instead of rolling. If heads, the roll was a success."
            }
            if (skill.id == skl.unshakableResolve && character!!.unshakableResolveUses > 0) {
                skills += "\nUnshakable Resolve skill - if all rolls (or flips) fail, you can choose to survive once per character. Make sure to adjust the value above if you use this skill."
            }
        }
        if (gamblerSkillsCount > 0) {
            skills += "\n$gamblerSkillsCount level(s) of gambler skills, allowing them to reroll dice or reflip coins and take the best result - once for each level of the skill."
        }
        if (skills.isNotEmpty()) {
            skills = "Relevant Skills:$skills"
        }
        return skills
    }

    private fun isPassedThreshold(): Boolean {
        val prev = character!!.infection.toInt()
        val cur = infection.valueTextField.text.toString().toInt()

        return (prev < 25 && cur >= 25) || (prev < 50 && cur >= 50) || (cur >= 75)
    }

    private fun hasRegressionOrRemission(relevantSkills: List<FullCharacterModifiedSkillModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id.equalsAnyOf(Constants.SpecificSkillIds.regressionTypeSkills)) {
                return true
            }
        }
        return false
    }

    private fun hasUnshakableResolve(relevantSkills: List<FullCharacterModifiedSkillModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id == Constants.SpecificSkillIds.unshakableResolve) {
                return true
            }
        }
        return false
    }

    private fun mysteriousStrangerTotal(relevantSkills: List<FullCharacterModifiedSkillModel>): Int {
        var count = 0
        for (skill in relevantSkills) {
            if (skill.id.equalsAnyOf(Constants.SpecificSkillIds.mysteriousStrangerTypeSkills)) {
                count++
            }
        }
        return count
    }

    private fun hasRegression(relevantSkills: List<FullCharacterModifiedSkillModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id == Constants.SpecificSkillIds.regression) {
                return true
            }
        }
        return false
    }

    private fun hasRemission(relevantSkills: List<FullCharacterModifiedSkillModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id == Constants.SpecificSkillIds.remission) {
                return true
            }
        }
        return false
    }

    private fun getReductionAmount(relevantSkills: List<FullCharacterModifiedSkillModel>): String {
        if (hasRemission(relevantSkills)) {
            return "1d4"
        }
        if (hasRegression(relevantSkills)) {
            return "1"
        }
        return ""
    }

}