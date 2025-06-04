package com.forkbombsquad.stillalivelarp.utils

class Constants {

    class CharacterTypeId {
        companion object {
            const val standard = 1
            const val NPC = 2
            const val planner = 3
            const val hidden = 4
        }
    }

    class URLs {
        companion object {
            const val rulebookUrl = "https://stillalivelarp.com/rulebook-app"
        }
    }

    class Logging {
        companion object {
            // TODO ALWAYS - set these to false before release
            const val showLogging = true
            const val showTestLogging = true
            const val showDebugButtonInAccountView = false
        }
    }

    class SkillTypes {
        companion object {
            const val combat = 1
            const val profession = 2
            const val talent = 3
        }
    }

    class SpecificSkillCategories {
        companion object {
            const val BEGINNER_SKILLS = 1
            const val THE_INFECTED = 13
            const val PRESTIGE = 14
            const val SPECIALIZATION = 15
        }
    }

    class GearTypes {
        companion object {
            const val meleeWeapon = "Melee Weapon"
            const val firearm = "Firearm"
            const val clothing = "Clothing"
            const val accessory = "Accessory"
            const val bag = "Bag"
            const val other = "Other"

            val allTypes: List<String> = listOf(meleeWeapon, firearm, clothing, accessory, bag, other)
        }
    }

    class GearPrimarySubtype {
        companion object {
            const val superLightMeleeWeapon = "Super Light Melee Weapon"
            const val lightMeleeWeapon = "Light Melee Weapon"
            const val mediumMeleeWeapon = "Medium Melee Weapon"
            const val heavyMeleeWeapon = "Heavy Melee Weapon"

            const val lightFirearm = "Light Firearm"
            const val mediumFirearm = "Medium Firearm"
            const val heavyFirearm = "Heavy Firearm"
            const val advancedFirearm = "Advanced Firearm"
            const val militaryGradeFirearm = "Military Grade Firearm"

            const val blacklightFlashlight = "Blacklight Flashlight"
            const val flashlight = "Regular Flashlight"

            const val smallBag = "Small Bag"
            const val mediumBag = "Medium Bag"
            const val largeBag = "Large Bag"
            const val extraLargeBag = "Extra Large Bag"

            const val other = "Other"
            const val none = "None"

            val allMeleeTypes: List<String> = listOf(superLightMeleeWeapon, lightMeleeWeapon, mediumMeleeWeapon, heavyMeleeWeapon)
            val allFirearmTypes: List<String> = listOf(lightFirearm, mediumFirearm, heavyFirearm, advancedFirearm, militaryGradeFirearm)
            var allClothingTypes: List<String> = listOf(none)
            var allAccessoryTypes: List<String> = listOf(blacklightFlashlight, flashlight, other)
            var allBagTypes: List<String> = listOf(smallBag, mediumBag, largeBag, extraLargeBag)
            var allOtherTypes: List<String> = listOf(other)
        }
    }

    class GearSecondarySubtype {
        companion object {
            const val none = "None"
            const val primaryFirearm = "Primary Firearm"

            val allFirearmTypes: List<String> = listOf(none, primaryFirearm)
            val allNonFirearmTypes: List<String> = listOf(none)
        }
    }

    class SpecificSkillIds {
        companion object {
            const val combatAficionado_T = 11
            const val combatSpecialist_P = 12
            const val expertCombat = 19
            const val professionAficionado_T = 63
            const val professionSpecialist_C = 64
            const val expertProfession = 20
            const val talentAficionado_C = 74
            const val talentSpecialist_P = 75
            const val expertTalent = 21

            // Adaptable Type
            const val adaptable = 1
            const val extremelyAdaptable = 23

            // Deep Pockets Type
            const val bandoliers = 5
            const val parachutePants = 61
            const val deeperPockets = 16
            const val deepPockets = 15

            // Investigator Type
            const val investigator = 38
            const val interrogator = 37

            // Tough Skin Type
            const val toughSkin = 80
            const val painTolerance = 60
            const val naturalArmor = 55
            const val scaledSkin = 70

            // Walk like a zombie type
            const val deadManStanding = 13
            const val deadManWalking = 14

            // Gambler type
            const val gamblersLuck = 29
            const val gamblersEye = 27

            // Regression type
            const val regression = 68
            const val remission = 69

            // Will to live type
            const val willToLive = 93
            const val unshakableResolve = 89

            // Mysterious Stranger type
            const val mysteriousStranger = 54
            const val unknownAssailant = 88
            const val annonomousAlly = 4

            // Plot Armor Type
            const val plotArmor = 96

            // Fully Loaded Type
            const val fullyLoaded = 100

            // Fortune Skills
            const val fortunateFind = 97
            const val prosperousDiscovery = 98

            val allSpecalistSkills: Array<Int> = arrayOf(combatAficionado_T, combatSpecialist_P, expertCombat, professionAficionado_T, professionSpecialist_C, expertProfession, talentAficionado_C, talentSpecialist_P, expertTalent)
            
            val allLevel2SpecialistSkills: Array<Int> = arrayOf(combatAficionado_T, combatSpecialist_P, professionAficionado_T, professionSpecialist_C, talentAficionado_C, talentSpecialist_P)
            val allSpecalistsNotUnderExpertCombat: Array<Int> = arrayOf(combatSpecialist_P, professionSpecialist_C, talentAficionado_C, combatAficionado_T, expertTalent, expertProfession)
            val allSpecalistsNotUnderExpertProfession: Array<Int> = arrayOf(professionSpecialist_C, professionAficionado_T, combatSpecialist_P, talentSpecialist_P, expertCombat, expertTalent)
            val allSpecalistsNotUnderExpertTalent: Array<Int> = arrayOf(talentSpecialist_P, talentAficionado_C, professionAficionado_T, combatAficionado_T, expertCombat, expertProfession)
            val allCombatReducingSkills: Array<Int> = arrayOf(combatAficionado_T, combatSpecialist_P, expertCombat)
            val allCombatIncreasingSkills: Array<Int> = arrayOf(professionSpecialist_C, talentAficionado_C)
            val allProfessionReducingSkills: Array<Int> = arrayOf(professionSpecialist_C, professionAficionado_T, expertProfession)
            val allProfessionIncreasingSkills: Array<Int> = arrayOf(combatSpecialist_P, talentSpecialist_P)
            val allTalentReducingSkills: Array<Int> = arrayOf(talentSpecialist_P, talentAficionado_C, expertTalent)
            val allTalentIncreasingSkills: Array<Int> = arrayOf(combatAficionado_T, professionAficionado_T)

            val deepPocketTypeSkills: Array<Int> = arrayOf(bandoliers, parachutePants, deeperPockets, deepPockets)
            val investigatorTypeSkills: Array<Int> = arrayOf(investigator, interrogator)
            val toughSkinTypeSkills: Array<Int> = arrayOf(toughSkin, painTolerance, naturalArmor, scaledSkin, plotArmor)
            val toughSkinTypeSkillsWithoutScaledSkin: Array<Int> = arrayOf(toughSkin, painTolerance, naturalArmor)
            val walkLikeAZombieTypeSkills: Array<Int> = arrayOf(deadManStanding, deadManWalking)
            val gamblerTypeSkills: Array<Int> = arrayOf(gamblersLuck, gamblersEye)
            val regressionTypeSkills: Array<Int> = arrayOf(regression, remission)
            val willToLiveTypeSkills: Array<Int> = arrayOf(willToLive, unshakableResolve)
            val mysteriousStrangerTypeSkills: Array<Int> = arrayOf(mysteriousStranger, unknownAssailant, annonomousAlly)

            val fortuneSkills: Array<Int> = arrayOf(fortunateFind, prosperousDiscovery)

            val barcodeRelevantSkills: Array<Int> = deepPocketTypeSkills + investigatorTypeSkills + toughSkinTypeSkills + walkLikeAZombieTypeSkills + gamblerTypeSkills + regressionTypeSkills + willToLiveTypeSkills + mysteriousStrangerTypeSkills + fortuneSkills + arrayOf(
                fullyLoaded)
            val checkInRelevantSkillsOnly = deepPocketTypeSkills + toughSkinTypeSkills + walkLikeAZombieTypeSkills + investigatorTypeSkills + gamblerTypeSkills + fortuneSkills + arrayOf(
                fullyLoaded)
        }
    }
}