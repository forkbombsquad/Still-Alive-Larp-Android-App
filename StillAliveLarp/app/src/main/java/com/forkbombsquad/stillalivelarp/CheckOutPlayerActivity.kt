package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
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

    private val barcodeScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            globalFromJson<PlayerCheckOutBarcodeModel>(result.contents).ifLet({
                DataManager.shared.playerCheckOutModel = it
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

    private fun setupView() {

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
            DataManager.shared.playerCheckOutModel.ifLet { barcodeModel ->
                val player = barcodeModel.player
                val character = barcodeModel.character
                val eventId = barcodeModel.eventId
                val relevantSkills = barcodeModel.relevantSkills
                val isNpc = character == null

                val valResult = validateFields()
                if (!valResult.hasError) {
                    character.ifLet({ char ->
                        if (isPassedThreshold(char) && isAlive.valuePickerView.selectedItemPosition == 0) {
                            AlertUtils.displayMessage(
                                context = this,
                                title = "Warning!",
                                message = "${char.fullName} has pased an infeciton threshold! Make sure to make the check to see if they turn into a zombie!\n\n${getThresholdCheckSkillsList(relevantSkills, char)}",
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
        }
        buildView()
    }

    private fun checkoutStepOne() {
        DataManager.shared.playerCheckOutModel.ifLet { barcodeModel ->
            val isNpc = barcodeModel.character == null
            val relevantSkills = barcodeModel.relevantSkills

            if (!isNpc && isAlive.valuePickerView.selectedItemPosition == 1) {
                AlertUtils.displayMessage(
                    context = this,
                    title = "Warning!",
                    message = "${barcodeModel.character?.fullName ?: ""} has seemingly perished, but they still have a chance! Make sure you roll 1d10 to see if they miraculously survive (by rolling a 10)!\n\n${getDeathCheckSkillsList(relevantSkills)}",
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
    }

    private fun checkoutStepTwo() {
        DataManager.shared.playerCheckOutModel.ifLet { barcodeModel ->
            val player = barcodeModel.player
            val character = barcodeModel.character
            val eventId = barcodeModel.eventId
            val relevantSkills = barcodeModel.relevantSkills
            val isNpc = character == null

            character.ifLet({ char ->
                checkoutButton.setLoadingWithText("Loading Character")
                val characterRequest = CharacterService.GetCharacter()
                lifecycleScope.launch {
                    characterRequest.successfulResponse(IdSP(char.id)).ifLet({ cm ->
                        val editedChar = CharacterModel(
                            id = cm.id,
                            fullName = cm.fullName,
                            startDate = cm.startDate,
                            isAlive = (isAlive.valuePickerView.selectedItemPosition == 0).ternary("TRUE", "FALSE"),
                            deathDate = (isAlive.valuePickerView.selectedItemPosition == 0).ternary("", LocalDate.now().yyyyMMddFormatted()),
                            infection = infection.getValue(),
                            bio = cm.bio,
                            approvedBio = cm.approvedBio,
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
                        checkoutButton.setLoading(false)
                        restartScanner()
                    })
                }
            }, {
                checkoutStepThree()
            })
        }
    }

    private fun checkoutStepThree() {
        DataManager.shared.playerCheckOutModel.ifLet { barcodeModel ->
            val player = barcodeModel.player
            val character = barcodeModel.character
            val eventId = barcodeModel.eventId
            val relevantSkills = barcodeModel.relevantSkills
            val isNpc = character == null

            var needToAwardExtraXp = false
            if (!isNpc && isAlive.valuePickerView.selectedItemPosition == 1) {
                needToAwardExtraXp = true
            }
            val xpAmount = isNpc.ternary(2, 1)

            checkoutButton.setLoadingWithText("Loading Player")
            val playerRequest = PlayerService.GetPlayer()
            lifecycleScope.launch {
               playerRequest.successfulResponse(IdSP(player.id)).ifLet({ fullPlayer ->
                   val xp = fullPlayer.experience.toInt() + xpAmount
                   val events = fullPlayer.numEventsAttended.toInt() + 1
                   val npcEvents = fullPlayer.numNpcEventsAttended.toInt() + isNpc.ternary(1, 0)

                   val playerUpdate = PlayerModel(
                       id = fullPlayer.id,
                       username = fullPlayer.username,
                       fullName =  fullPlayer.fullName,
                       startDate = fullPlayer.startDate,
                       experience = xp.toString(),
                       freeTier1Skills = fullPlayer.freeTier1Skills,
                       prestigePoints = fullPlayer.prestigePoints,
                       isCheckedIn = "FALSE",
                       isCheckedInAsNpc = "FALSE",
                       lastCheckIn = LocalDate.now().yyyyMMddFormatted(),
                       numEventsAttended = events.toString(),
                       numNpcEventsAttended = npcEvents.toString(),
                       isAdmin = fullPlayer.isAdmin
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
               }, {
                   checkoutButton.setLoading(false)
                   restartScanner()
               })
            }
        }
    }

    private fun checkoutStepFour(needToAwardExtraXp: Boolean) {
        DataManager.shared.playerCheckOutModel.ifLet { barcodeModel ->
            val player = barcodeModel.player
            val character = barcodeModel.character
            val eventId = barcodeModel.eventId
            val eventAttendeeId = barcodeModel.eventAttendeeId
            val relevantSkills = barcodeModel.relevantSkills
            val isNpc = character == null

            checkoutButton.setLoadingWithText("Updating Records")

            val eventAttendeeUpdate = EventAttendeeModel(
                id = eventAttendeeId,
                playerId = player.id,
                characterId = character?.id,
                eventId = eventId,
                isCheckedIn = "FALSE",
                asNpc = isNpc.ternary("TRUE", "FALSE")
            )

            val updateAttendeeRequest = AdminService.UpdateEventAttendee()
            lifecycleScope.launch {
                updateAttendeeRequest.successfulResponse(UpdateModelSP(eventAttendeeUpdate)).ifLet({ eventAttendee ->
                    if (needToAwardExtraXp && !isNpc) {
                        checkoutButton.setLoadingWithText("Loading Character For Death Xp Bonus")
                        val characterRequest = CharacterService.GetCharacter()
                        lifecycleScope.launch {
                            characterRequest.successfulResponse(IdSP(character!!.id)).ifLet({ characterModel ->
                                checkoutButton.setLoadingWithText("Loading Skills For Death Xp Bonus")
                                characterModel.getAllXpSpent(lifecycleScope) { xp ->
                                    var adjustedXp = xp / 2

                                    var max = player.numEventsAttended.toInt()
                                    max += player.numNpcEventsAttended.toInt() // Adding double xp for npc events

                                    adjustedXp = min(max, adjustedXp)

                                    checkoutButton.setLoadingWithText("Refunding Xp")

                                    val award = AwardCreateModel.createPlayerAward(
                                        playerId = player.id,
                                        awardType = AwardPlayerType.XP,
                                        reason = "Death of Character: ${characterModel.fullName}",
                                        amount = adjustedXp.toString()
                                    )
                                    val createAwardRequest = AdminService.AwardPlayer()
                                    lifecycleScope.launch {
                                        createAwardRequest.successfulResponse(AwardCreateSP(award)).ifLet({ _ ->
                                            characterModel.getAllPrestigePointsSpent(lifecycleScope) { pp ->
                                                if (pp > 0) {
                                                    checkoutButton.setLoadingWithText("Refunding Xp")

                                                    val a = AwardCreateModel.createPlayerAward(
                                                        playerId = player.id,
                                                        awardType = AwardPlayerType.PRESTIGEPOINTS,
                                                        reason = "Death of Character: ${characterModel.fullName}",
                                                        amount = pp.toString()
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
                                            }

                                        }, {
                                            checkoutButton.setLoading(false)
                                            showSuccessAlertAllowingRescan("Successfully Checked Out ${player.fullName}!\nBut unable to award death xp!")
                                        })
                                    }
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
        DataManager.shared.playerCheckOutModel.ifLet {
            val player = it.player
            val character = it.character
            val eventId = it.eventId
            val relevantSkills = it.relevantSkills
            val isNpc = character == null

            playerName.set(player.fullName)
            totalEventsAttended.set("${player.numEventsAttended}+1")
            totalNpcEventsAttended.set(isNpc.ternary("${player.numNpcEventsAttended}+1", player.numNpcEventsAttended))
            lastEventAttended.set(LocalDate.now().yyyyMMddFormatted().yyyyMMddToMonthDayYear())

            // Character Section
            character.ifLet({ char ->
                characterLayout.isGone = false
                characterName.set(char.fullName)
                infection.set(char.infection)
                reduceInfection.isGone = true
                infection.div.isGone = false

                if (hasRegressionOrRemission(relevantSkills)) {
                    infection.div.isGone = true
                    reduceInfection.isGone = false
                    reduceInfection.set(getReductionAmount(relevantSkills))
                }

                val mysterStrangerTotal = mysteriousStrangerTotal(relevantSkills)
                mysteriousStranger.isGone = mysterStrangerTotal == 0
                if (mysterStrangerTotal > 0) {
                    mysteriousStranger.set("Mysterious Stranger Uses (out of $mysterStrangerTotal)", char.mysteriousStrangerUses)
                }
                val hasUnshakableResolve = hasUnshakableResolve(relevantSkills)
                unshakableResolve.isGone = !hasUnshakableResolve
                if (hasUnshakableResolve) {
                    unshakableResolve.set(char.unshakableResolveUses)
                }

                bullets.set(char.bullets)
                megas.set(char.megas)
                rivals.set(char.rivals)
                rockets.set(char.rockets)
                casings.set(char.bulletCasings)
                cloth.set(char.clothSupplies)
                wood.set(char.woodSupplies)
                metal.set(char.metalSupplies)
                tech.set(char.techSupplies)
                medical.set(char.medicalSupplies)

            }, {
                characterLayout.isGone = true
                characterName.set("NPC")
            })
        }
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

    private fun getDeathCheckSkillsList(relevantSkills: Array<SkillBarcodeModel>): String {
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

    private fun getThresholdCheckSkillsList(relevantSkills: Array<SkillBarcodeModel>, char: CharacterBarcodeModel): String {
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
            if (skill.id == skl.unshakableResolve && char.unshakableResolveUses.toInt() > 0) {
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

    private fun isPassedThreshold(char: CharacterBarcodeModel): Boolean {
        val prev = char.infection.toInt()
        val cur = infection.valueTextField.text.toString().toInt()

        return (prev < 25 && cur >= 25) || (prev < 50 && cur >= 50) || (cur >= 75)
    }

    private fun hasRegressionOrRemission(relevantSkills: Array<SkillBarcodeModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id.equalsAnyOf(Constants.SpecificSkillIds.regressionTypeSkills)) {
                return true
            }
        }
        return false
    }

    private fun hasUnshakableResolve(relevantSkills: Array<SkillBarcodeModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id == Constants.SpecificSkillIds.unshakableResolve) {
                return true
            }
        }
        return false
    }

    private fun mysteriousStrangerTotal(relevantSkills: Array<SkillBarcodeModel>): Int {
        var count = 0
        for (skill in relevantSkills) {
            if (skill.id.equalsAnyOf(Constants.SpecificSkillIds.regressionTypeSkills)) {
                count++
            }
        }
        return count
    }

    private fun hasRegression(relevantSkills: Array<SkillBarcodeModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id == Constants.SpecificSkillIds.regression) {
                return true
            }
        }
        return false
    }

    private fun hasRemission(relevantSkills: Array<SkillBarcodeModel>): Boolean {
        for (skill in relevantSkills) {
            if (skill.id == Constants.SpecificSkillIds.remission) {
                return true
            }
        }
        return false
    }

    private fun getReductionAmount(relevantSkills: Array<SkillBarcodeModel>): String {
        if (hasRemission(relevantSkills)) {
            return "1d4"
        }
        if (hasRegression(relevantSkills)) {
            return "1"
        }
        return ""
    }

}