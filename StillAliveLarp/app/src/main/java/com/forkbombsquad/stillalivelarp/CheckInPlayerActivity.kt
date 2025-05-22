package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckInBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.SkillBarcodeModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCheckInSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.GiveCharacterCheckInRewardsSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.CaptureActivityPortrait
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.GearCell
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.decompress
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class CheckInPlayerActivity : NoStatusBarActivity() {

    private lateinit var playerName: KeyValueView
    private lateinit var totalEventsAttended: KeyValueView
    private lateinit var totalNpcEventsAttended: KeyValueView
    private lateinit var lastEventAttended: KeyValueView

    private lateinit var characterName: KeyValueView
    private lateinit var characterRaffle: KeyValueView

    private lateinit var characterLayout: LinearLayout
    private lateinit var infection: KeyValueView
    private lateinit var infectionThreshold: KeyValueView
    private lateinit var bullets: KeyValueView
    private lateinit var megas: KeyValueView
    private lateinit var rivals: KeyValueView
    private lateinit var rockets: KeyValueView
    private lateinit var casings: KeyValueView
    private lateinit var cloth: KeyValueView
    private lateinit var wood: KeyValueView
    private lateinit var metal: KeyValueView
    private lateinit var tech: KeyValueView
    private lateinit var medical: KeyValueView
    private lateinit var armor: KeyValueView
    private lateinit var armorBeadCount: KeyValueView

    private lateinit var skillsLayout: LinearLayout
    private lateinit var ammoSkills: KeyValueView
    private lateinit var intrigueSkills: KeyValueView
    private lateinit var regularArmorSkills: KeyValueView
    private lateinit var regularArmorSkillBeadCount: KeyValueView
    private lateinit var bulletproofArmorSkills: KeyValueView
    private lateinit var bulletproofArmorSkillBeadCount: KeyValueView
    private lateinit var plotArmorSkills: KeyValueView
    private lateinit var plotArmorSkillBeadCount: KeyValueView
    private lateinit var disguiseSkills: KeyValueView
    private lateinit var disguiseSkillBeadCount: KeyValueView
    private lateinit var gamblerSkills: KeyValueView
    private lateinit var gamblerSkillRaffleTicketCount: KeyValueView
    private lateinit var fortuneSkill: KeyValueView
    private lateinit var fortuneSkillBonusMaterials: KeyValueView
    private lateinit var fullyLoadedSkill: KeyValueView
    private lateinit var fullyLoadedDetails: KeyValueView

    private lateinit var eventName: KeyValueView

    private lateinit var gearLayout: LinearLayout
    private lateinit var addNewGearButton: NavArrowButtonGreen
    private lateinit var gearListLayout: LinearLayout

    private lateinit var checkInButton: LoadingButton

    private var gearModified = false

    private val barcodeScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            globalFromJson<PlayerCheckInBarcodeModel>(result.contents.decompress()).ifLet({
                OldDataManager.shared.playerCheckInModel = it
                buildView()
            }, {
                AlertUtils.displayError(this, "Unable to parse barcode data!") { _, _ -> }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_player)
        setupView()
    }

    private fun setupView() {

        playerName = findViewById(R.id.checkinplayer_playerName)
        totalEventsAttended = findViewById(R.id.checkinplayer_totalEvents)
        totalNpcEventsAttended = findViewById(R.id.checkinplayer_totalNpcEvents)
        lastEventAttended = findViewById(R.id.checkinplayer_lastEvent)

        characterName = findViewById(R.id.checkinplayer_characterName)
        characterRaffle = findViewById(R.id.checkinplayer_npcRaffleTicketCount)

        characterLayout = findViewById(R.id.checkinplayer_characterLayout)
        infection = findViewById(R.id.checkinplayer_infection)
        infectionThreshold = findViewById(R.id.checkinplayer_infectionThreshold)
        bullets = findViewById(R.id.checkinplayer_bullets)
        megas = findViewById(R.id.checkinplayer_megas)
        rivals = findViewById(R.id.checkinplayer_rivals)
        rockets = findViewById(R.id.checkinplayer_rockets)
        casings = findViewById(R.id.checkinplayer_casings)
        cloth = findViewById(R.id.checkinplayer_cloth)
        wood = findViewById(R.id.checkinplayer_wood)
        metal = findViewById(R.id.checkinplayer_metal)
        tech = findViewById(R.id.checkinplayer_tech)
        medical = findViewById(R.id.checkinplayer_medical)
        armor = findViewById(R.id.checkinplayer_armor)
        armorBeadCount = findViewById(R.id.checkinplayer_armorBeadCount)

        skillsLayout = findViewById(R.id.checkinplayer_relevantSkillsLayout)
        ammoSkills = findViewById(R.id.checkinplayer_ammoSkills)
        intrigueSkills = findViewById(R.id.checkinplayer_intrigueSkills)
        regularArmorSkills = findViewById(R.id.checkinplayer_regularArmorSkills)
        regularArmorSkillBeadCount = findViewById(R.id.checkinplayer_regularArmorSkillBeadCount)
        bulletproofArmorSkills = findViewById(R.id.checkinplayer_bulletproofArmorSkills)
        bulletproofArmorSkillBeadCount = findViewById(R.id.checkinplayer_bulletproofArmorSkillBeadCount)
        plotArmorSkills = findViewById(R.id.checkinplayer_plotArmorSkills)
        plotArmorSkillBeadCount = findViewById(R.id.checkinplayer_plotArmorSkillBeadCount)
        disguiseSkills = findViewById(R.id.checkinplayer_disguiseSkills)
        disguiseSkillBeadCount = findViewById(R.id.checkinplayer_disguiseSkillBeadCount)
        gamblerSkills = findViewById(R.id.checkinplayer_gamblerSkills)
        gamblerSkillRaffleTicketCount = findViewById(R.id.checkinplayer_gamblerSkillRaffleTicketCount)
        fortuneSkill = findViewById(R.id.checkinplayer_fortuneSkill)
        fortuneSkillBonusMaterials = findViewById(R.id.checkinplayer_fortuneSkillBonusMaterials)
        fullyLoadedSkill = findViewById(R.id.checkinplayer_fullyLoadedSkill)
        fullyLoadedDetails = findViewById(R.id.checkinplayer_fullyLoadedDetails)

        gearLayout = findViewById(R.id.checkinplayer_gearLayout)
        addNewGearButton = findViewById(R.id.checkinplayer_addNewGear)
        gearListLayout = findViewById(R.id.checkinplayer_gearListLayout)

        eventName = findViewById(R.id.checkinplayer_eventName)

        checkInButton = findViewById(R.id.checkinplayer_checkInButton)

        checkInButton.setOnClick {
            if (gearModified) {
                saveModifiedGear {
                    checkIn()
                }
            } else {
                checkIn()
            }
        }

        addNewGearButton.setOnClick {
            OldDataManager.shared.gearToEdit = null
            OldDataManager.shared.unrelaltedUpdateCallback = {
                gearModified = true
                buildView()
            }
            val intent = Intent(this, AddEditGearFromBarcodeActivity::class.java)
            startActivity(intent)
        }

        buildView()
    }

    private fun saveModifiedGear(finished: () -> Unit) {
        checkInButton.setLoadingWithText("Organizing Gear...")
        val gear = OldDataManager.shared.playerCheckInModel?.gear
        if (gear != null) {
            if (gear.id == -1) {
                // Create New List
                val createModel = GearCreateModel(gear.characterId, gear.gearJson)
                val request = AdminService.CreateGear()
                checkInButton.setLoadingWithText("Creating Gear Listing...")
                lifecycleScope.launch {
                    request.successfulResponse(CreateModelSP(createModel)).ifLet { newGearModel ->
                        OldDataManager.shared.playerCheckInModel?.gear = newGearModel
                        checkInButton.setLoadingWithText("Gear Added!")
                        finished()
                    }
                }
            } else {
                // update Existing
                val request = AdminService.UpdateGear()
                checkInButton.setLoadingWithText("Updating Gear...")
                lifecycleScope.launch {
                    request.successfulResponse(UpdateModelSP(gear)).ifLet { updatedGearModel ->
                        OldDataManager.shared.playerCheckInModel?.gear = updatedGearModel
                        checkInButton.setLoadingWithText("Gear Updated!")
                        finished()
                    }
                }
            }
        }
        gearModified = false
    }
    private fun checkIn() {
        OldDataManager.shared.playerCheckInModel.ifLet { checkIn ->
            val player = checkIn.player
            val character = checkIn.character
            val event = checkIn.event
            val relevantSkills = checkIn.relevantSkills

            val isNpc = character == null

            checkInButton.setLoadingWithText("Checking in Player...")
            // DO NOT SET THE CHAR ID, The service will do that later
            val eventAttendeeCreate = EventAttendeeCreateModel(
                playerId = player.id,
                characterId = null,
                eventId = event.id,
                isCheckedIn = "TRUE",
                asNpc = isNpc.ternary("TRUE", "FALSE")
            )

            val checkInPlayerRequest = AdminService.CheckInPlayer()
            lifecycleScope.launch {
                checkInPlayerRequest.successfulResponse(CreateModelSP(eventAttendeeCreate)).ifLet({ _ ->
                    character.ifLet({ char ->
                        checkInButton.setLoadingWithText("Adding Bullets...")
                        var bullets = getAdditionalBulletCount(relevantSkills)
                        bullets += char.bullets.toInt()
                        val giveCharCheckInRewardsRequest = AdminService.GiveCharacterCheckInRewards()
                        lifecycleScope.launch {
                            giveCharCheckInRewardsRequest.successfulResponse(
                                GiveCharacterCheckInRewardsSP(
                                    eventId = event.id,
                                    playerId = player.id,
                                    characterId = char.id,
                                    newBulletCount = bullets
                                )
                            ).ifLet({ _ ->
                                checkInButton.setLoadingWithText("Checking In Character...")
                                val checkInCharacterRequest = AdminService.CheckInCharacter()
                                lifecycleScope.launch {
                                    checkInCharacterRequest.successfulResponse(
                                        CharacterCheckInSP(
                                            eventId = event.id,
                                            playerId = player.id,
                                            characterId = char.id
                                        )
                                    ).ifLet({
                                        checkInButton.setLoading(false)
                                        showSuccessAlertAllowingRescan("${player.fullName} checked in as ${char.fullName}!")
                                    }, {
                                        checkInButton.setLoading(false)
                                        restartScanner()
                                    })
                                }
                            }, {
                                checkInButton.setLoading(false)
                                restartScanner()
                            })
                        }
                    }, {
                        checkInButton.setLoading(false)
                        showSuccessAlertAllowingRescan("${player.fullName} checked in as NPC!")
                    })
                }, {
                    checkInButton.setLoading(false)
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
        OldDataManager.shared.playerCheckInModel.ifLet {
            val player = it.player
            val character = it.character
            val event = it.event
            val relevantSkills = it.relevantSkills
            val gear = it.gear
            var gearList: Map<String, List<GearJsonModel>> = mapOf()
            if (gear != null) {
                OldDataManager.shared.selectedCharacterGear = arrayOf(gear)
                gearList = OldDataManager.shared.getGearOrganzied()
            }


            val isNpc = character == null

            // Player Section
            playerName.set(player.fullName)
            totalEventsAttended.set("${player.numEventsAttended}+1")
            totalNpcEventsAttended.set(isNpc.ternary("${player.numNpcEventsAttended}+1", player.numNpcEventsAttended))
            lastEventAttended.set(player.lastCheckIn.yyyyMMddToMonthDayYear())

            // Character Section
            character.ifLet({ char ->
                characterRaffle.isGone = true
                characterLayout.isGone = false
                characterName.set(char.fullName)
                val inf = char.infection.toInt()
                infection.set("${inf}%", showDiv = (inf < 25))
                infectionThreshold.isGone = false
                if (inf >= 75) {
                    infectionThreshold.set("THIRD")
                } else if (inf >= 50) {
                    infectionThreshold.set("SECOND")
                } else if (inf >= 25) {
                    infectionThreshold.set("FIRST")
                } else {
                    infectionThreshold.isGone = true
                }
                bullets.set("${char.bullets}+${getAdditionalBulletCount(relevantSkills)}")
                megas.set(char.megas)
                rivals.set(char.rivals)
                rockets.set(char.rockets)
                casings.set(char.bulletCasings)
                cloth.set(char.clothSupplies)
                wood.set(char.woodSupplies)
                metal.set(char.metalSupplies)
                tech.set(char.techSupplies)
                medical.set(char.medicalSupplies)

                if (char.armor == CharacterArmor.NONE.text) {
                    armor.set(char.armor, false)
                    armorBeadCount.isGone = true
                } else {
                    armor.set(char.armor, false) // Hide div
                    armorBeadCount.isGone = false

                    if (char.armor == CharacterArmor.METAL.text) {
                        armorBeadCount.valueView.setTextColor(getColor(R.color.blue))
                        armorBeadCount.set("BLUE BEADS NEEDED", "1")
                    } else if (char.armor == CharacterArmor.BULLETPROOF.text) {
                        armorBeadCount.valueView.setTextColor(getColor(R.color.mid_red))
                        armorBeadCount.set("RED BEADS NEEDED", "1")
                    }
                }
            }, {
                characterLayout.isGone = true
                characterName.set("NPC")
                characterRaffle.isGone = false
            })

            // Relevant Skills Section
            if (!hasRelevantSkills(relevantSkills)) {
                skillsLayout.isGone = true
            } else {
                skillsLayout.isGone = false

                ammoSkills.setAndHideIfEmpty(getAmmoSkillNames(relevantSkills))

                intrigueSkills.setAndHideIfEmpty(getIntrigueSkillNames(relevantSkills))

                regularArmorSkills.setAndHideIfEmpty(getRegularArmorSkillNames(relevantSkills))
                val regularArmorBeads = getToughSkinBlueBeadCount(relevantSkills)
                regularArmorSkillBeadCount.isGone = regularArmorBeads == 0
                regularArmorSkillBeadCount.set(regularArmorBeads.toString())
                regularArmorSkillBeadCount.valueView.setTextColor(getColor(R.color.blue))

                val scaledSkin = getScaledSkin(relevantSkills)
                scaledSkin.ifLet({ ss ->
                    bulletproofArmorSkills.isGone = false
                    bulletproofArmorSkillBeadCount.isGone = false
                    bulletproofArmorSkillBeadCount.valueView.setTextColor(getColor(R.color.mid_red))
                    bulletproofArmorSkills.set(ss.name)
                }, {
                    bulletproofArmorSkills.isGone = true
                    bulletproofArmorSkillBeadCount.isGone = true
                })

                val plotArmor = getPlotArmor(relevantSkills)
                plotArmor.ifLet({ pa ->
                    plotArmorSkills.isGone = false
                    plotArmorSkillBeadCount.isGone = false
                    plotArmorSkills.set(pa.name)
                }, {
                    plotArmorSkills.isGone = true
                    plotArmorSkillBeadCount.isGone = true
                })

                disguiseSkills.setAndHideIfEmpty(getDisguiseSkillNames(relevantSkills))
                val disguiseBeads = getDisguiseGreenBeadCount(relevantSkills)
                disguiseSkillBeadCount.isGone = disguiseBeads == 0
                disguiseSkillBeadCount.set(disguiseBeads.toString())
                disguiseSkillBeadCount.valueView.setTextColor(getColor(R.color.green))

                gamblerSkills.setAndHideIfEmpty(getGamblerSkillNames(relevantSkills))
                val raffleTickets = getBonusRaffleTicketCount(relevantSkills)
                gamblerSkillRaffleTicketCount.isGone = raffleTickets == 0
                gamblerSkillRaffleTicketCount.set(raffleTickets.toString())
                gamblerSkillRaffleTicketCount.valueView.setTextColor(getColor(R.color.green))

                fortuneSkill.setAndHideIfEmpty(getFortuneSkillName(relevantSkills))
                fortuneSkillBonusMaterials.setAndHideIfEmpty(getFortuneText(relevantSkills))

                val fullyLoaded = getFullyLoaded(relevantSkills)
                fullyLoaded.ifLet({ fl ->
                    fullyLoadedSkill.isGone = false
                    gear?.getPrimaryFirearm().ifLet({ pf ->
                        fullyLoadedSkill.set("${pf.name}\n${pf.desc}", showDiv = false)
                        fullyLoadedDetails.isGone = false
                    }, {
                        fullyLoadedSkill.set("!! No Primary Firearm Registered !!", showDiv = true)
                        fullyLoadedDetails.isGone = true
                    })
                }, {
                    fullyLoadedSkill.isGone = true
                    fullyLoadedDetails.isGone = true
                })

            }

            // Event Section
            eventName.set(event.title)

            gearLayout.isGone = isNpc
            gearListLayout.removeAllViews()
            gearList.forEach { (key, list) ->
                val textView = TextView(this)
                val tvParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                tvParams.setMargins(0, 8, 0, 8)
                textView.layoutParams = tvParams
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                textView.setTypeface(null, Typeface.BOLD)
                textView.setTextColor(Color.BLACK)
                textView.text = key
                gearListLayout.addView(textView)

                list.forEach { g ->
                    val gearCell = GearCell(this)
                    gearCell.setup(g)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 8, 0, 8)
                    gearCell.layoutParams = params
                    gearCell.setOnClick {
                        OldDataManager.shared.gearToEdit = g
                        OldDataManager.shared.unrelaltedUpdateCallback = {
                            gearModified = true

                            buildView()
                        }
                        val intent = Intent(this, AddEditGearFromBarcodeActivity::class.java)
                        startActivity(intent)
                    }
                    gearListLayout.addView(gearCell)
                }
            }
            if (gearModified) {
                checkInButton.textView.text = "Save Gear Modifications\nAnd\nCheck In"
            } else {
                checkInButton.textView.text = "Check In"
            }
        }
    }

    private fun hasRelevantSkills(relevantSkills: Array<SkillBarcodeModel>): Boolean {
        for (sk in relevantSkills) {
            if (sk.id.equalsAnyOf(Constants.SpecificSkillIds.checkInRelevantSkillsOnly)) {
                return true
            }
        }
        return false
    }

    private fun getAdditionalBulletCount(relevantSkills: Array<SkillBarcodeModel>): Int {
        var bullets = 2
        for (sk in relevantSkills) {
            if (sk.id.equalsAnyOf(Constants.SpecificSkillIds.deepPocketTypeSkills)) {
                bullets += 2
            }
        }
        return bullets
    }

    private fun getToughSkinBlueBeadCount(relevantSkills: Array<SkillBarcodeModel>): Int {
        var count = 0
        for (sk in relevantSkills) {
            if (sk.id.equalsAnyOf(Constants.SpecificSkillIds.toughSkinTypeSkillsWithoutScaledSkin)) {
                count++
            }
        }
        return count
    }

    private fun getDisguiseGreenBeadCount(relevantSkills: Array<SkillBarcodeModel>): Int {
        var count = 0
        for (sk in relevantSkills) {
            if (sk.id.equalsAnyOf(Constants.SpecificSkillIds.walkLikeAZombieTypeSkills)) {
                count = 1
            }
        }
        return count
    }

    private fun getBonusRaffleTicketCount(relevantSkills: Array<SkillBarcodeModel>): Int {
        var count = 0
        for (sk in relevantSkills) {
            if (sk.id.equalsAnyOf(Constants.SpecificSkillIds.gamblerTypeSkills)) {
                count++
            }
        }
        return count
    }

    private fun getScaledSkin(relevantSkills: Array<SkillBarcodeModel>): SkillBarcodeModel? {
        return relevantSkills.firstOrNull { it.id == Constants.SpecificSkillIds.scaledSkin }
    }

    private fun getPlotArmor(relevantSkills: Array<SkillBarcodeModel>): SkillBarcodeModel? {
        return relevantSkills.firstOrNull { it.id == Constants.SpecificSkillIds.plotArmor }
    }

    private fun getFullyLoaded(relevantSkills: Array<SkillBarcodeModel>): SkillBarcodeModel? {
        return relevantSkills.firstOrNull { it.id == Constants.SpecificSkillIds.fullyLoaded }
    }

    private fun getSkillNames(relevantSkills: Array<SkillBarcodeModel>, skillIds: Array<Int>): String {
        var names = ""
        for (sk in relevantSkills) {
            if (sk.id.equalsAnyOf(skillIds)) {
                if (names.isNotEmpty()) {
                    names += ", "
                }
                names += sk.name
            }
        }
        return names
    }

    private fun getAmmoSkillNames(relevantSkills: Array<SkillBarcodeModel>): String {
        return getSkillNames(relevantSkills, Constants.SpecificSkillIds.deepPocketTypeSkills)
    }

    private fun getIntrigueSkillNames(relevantSkills: Array<SkillBarcodeModel>): String {
        return getSkillNames(relevantSkills, Constants.SpecificSkillIds.investigatorTypeSkills)
    }

    private fun getRegularArmorSkillNames(relevantSkills: Array<SkillBarcodeModel>): String {
        return getSkillNames(relevantSkills, Constants.SpecificSkillIds.toughSkinTypeSkillsWithoutScaledSkin)
    }

    private fun getDisguiseSkillNames(relevantSkills: Array<SkillBarcodeModel>): String {
        return getSkillNames(relevantSkills, Constants.SpecificSkillIds.walkLikeAZombieTypeSkills)
    }

    private fun getGamblerSkillNames(relevantSkills: Array<SkillBarcodeModel>): String {
        return getSkillNames(relevantSkills, Constants.SpecificSkillIds.gamblerTypeSkills)
    }

    private fun getFortuneSkillName(relevantSkills: Array<SkillBarcodeModel>): String {
        var first = getSkillNames(relevantSkills, arrayOf(Constants.SpecificSkillIds.prosperousDiscovery))
        return if (first.isEmpty()) {
            getSkillNames(relevantSkills, arrayOf(Constants.SpecificSkillIds.fortunateFind))
        } else {
            first
        }
    }

    private fun getFortuneText(relevantSkills: Array<SkillBarcodeModel>): String {
        var first = getSkillNames(relevantSkills, arrayOf(Constants.SpecificSkillIds.prosperousDiscovery))
        return if (first.isEmpty()) {
            if (getSkillNames(relevantSkills, arrayOf(Constants.SpecificSkillIds.fortunateFind)).isNotEmpty()) {
                "Roll Randomly for 1d4 Wood/Cloth/Metal or 1 Tech/Medical"
            } else {
                ""
            }
        } else {
            "Choose 2d4 Wood/Cloth/Metal or 2 Tech/Medical"
        }
    }
}