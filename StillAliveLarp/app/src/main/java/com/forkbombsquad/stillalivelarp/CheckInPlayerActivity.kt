package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCheckInSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.GiveCharacterCheckInRewardsSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class CheckInPlayerActivity : NoStatusBarActivity() {

    private lateinit var playerName: KeyValueView
    private lateinit var totalEventsAttended: KeyValueView
    private lateinit var totalNpcEventsAttended: KeyValueView
    private lateinit var lastEventAttended: KeyValueView

    private lateinit var characterName: KeyValueView

    private lateinit var characterLayout: LinearLayout
    private lateinit var infection: KeyValueView
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

    private lateinit var eventName: KeyValueView

    private lateinit var checkInButton: LoadingButton

    private val barcodeScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            globalFromJson<PlayerCheckInBarcodeModel>(result.contents).ifLet({
                DataManager.shared.playerCheckInModel = it
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
        setContentView(R.layout.activity_check_in_player)
        setupView()
    }

    private fun setupView() {

        playerName = findViewById(R.id.checkinplayer_playerName)
        totalEventsAttended = findViewById(R.id.checkinplayer_totalEvents)
        totalNpcEventsAttended = findViewById(R.id.checkinplayer_totalNpcEvents)
        lastEventAttended = findViewById(R.id.checkinplayer_lastEvent)

        characterName = findViewById(R.id.checkinplayer_characterName)

        characterLayout = findViewById(R.id.checkinplayer_characterLayout)
        infection = findViewById(R.id.checkinplayer_infection)
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

        eventName = findViewById(R.id.checkinplayer_eventName)

        checkInButton = findViewById(R.id.checkinplayer_checkInButton)

        checkInButton.setOnClick {
            DataManager.shared.playerCheckInModel.ifLet { checkIn ->
                val player = checkIn.player
                val character = checkIn.character
                val event = checkIn.event
                val relevantSkills = checkIn.relevantSkills

                var isNpc = character == null

                checkInButton.setLoadingWithText("Checking in player")
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
                            checkInButton.setLoadingWithText("Giving character rewards")
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
                                    checkInButton.setLoadingWithText("Checking in character")
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
        buildView()
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
        DataManager.shared.playerCheckInModel.ifLet {
            val player = it.player
            val character = it.character
            val event = it.event
            val relevantSkills = it.relevantSkills
            val primaryWeapon = it.primaryWeapon

            var isNpc = character == null

            // Player Section
            playerName.set(player.fullName)
            totalEventsAttended.set("${player.numEventsAttended}+1")
            totalNpcEventsAttended.set(isNpc.ternary("${player.numNpcEventsAttended}+1", player.numNpcEventsAttended))
            lastEventAttended.set(player.lastCheckIn.yyyyMMddToMonthDayYear())

            // Character Section
            character.ifLet({ char ->
                characterLayout.isGone = false
                characterName.set(char.fullName)
                infection.set(char.infection)
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
                    primaryWeapon.ifLet({ pm ->
                        fullyLoadedSkill.set("${pm.description} - ${pm.name}")
                    }, {
                        fullyLoadedSkill.set("MISSING PRIMARY WEAPON REGISTRATION")
                    })
                }, {
                    fullyLoadedSkill.isGone = true
                })

            }

            // Event Section
            eventName.set(event.title)
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
                count++
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