package com.forkbombsquad.stillalivelarp.utils

class Constants {

    class URLs {
        companion object {
            const val rulebookUrl = "https://stillalivelarp.com/rulebook"
        }
    }

    class Gear {
        companion object {
            const val primaryWeapon = "Primary Weapon"
        }
    }

    class Logging {
        companion object {
            const val showLogging = false
            // TODO ensure this is false before release
        }
    }

    class SkillTypes {
        companion object {
            const val combat = 1
            const val profession = 2
            const val talent = 3
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
            const val webOfInformants = 92

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
            const val gamblersTalent = 30
            const val gamblersEye = 27
            const val gamblersHeart = 28

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
            val investigatorTypeSkills: Array<Int> = arrayOf(investigator, interrogator, webOfInformants)
            val toughSkinTypeSkills: Array<Int> = arrayOf(toughSkin, painTolerance, naturalArmor, scaledSkin, plotArmor)
            val toughSkinTypeSkillsWithoutScaledSkin: Array<Int> = arrayOf(toughSkin, painTolerance, naturalArmor)
            val walkLikeAZombieTypeSkills: Array<Int> = arrayOf(deadManStanding, deadManWalking)
            val gamblerTypeSkills: Array<Int> = arrayOf(gamblersLuck, gamblersTalent, gamblersEye, gamblersHeart)
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