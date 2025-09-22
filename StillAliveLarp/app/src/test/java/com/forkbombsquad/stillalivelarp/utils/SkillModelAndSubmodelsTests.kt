package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.SkillModel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class SkillModelAndSubmodelsTests: BaseUnitTestClass {

    // Full Skill Model
    @ParameterizedTest(name = "SkillModel {0}, prereqs: {1}, postreqs: {2}, category: {3}")
    @MethodSource("skillProvider")
    fun testFullSkillModels(skillModel: SkillModel, prereqNames: List<String>, postreqNames: List<String>, categoryName: String)  = runTest {
        loadDataManagerHappyPath(this) {
            val skill = DataManager.shared.skills.firstOrNull { it.id == skillModel.id }
            assertNotNull(skill)
            assertEquals(skill!!.id, skillModel.id)
            assertEquals(skill.xpCost, skillModel.xpCost)
            assertEquals(skill.prestigeCost, skillModel.prestigeCost)
            assertEquals(skill.name, skillModel.name)
            assertEquals(skill.description, skillModel.description)
            assertEquals(skill.minInfection, skillModel.minInfection)
            assertEquals(skill.skillTypeId, skillModel.skillTypeId)
            assertEquals(skill.skillCategoryId, skillModel.skillCategoryId)

            if (prereqNames.isNotEmpty()) {
                assertNotEmpty(skill.prereqs)
                prereqNames.forEach { p ->
                    val prereq = skill.prereqs.firstOrNull { it.name == p }
                    assertNotNull(prereq)
                    assertEquals(prereq!!.name, p)
                }
            } else {
                assertEmpty(skill.prereqs)
            }

            if (postreqNames.isNotEmpty()) {
                assertNotEmpty(skill.postreqs)
                postreqNames.forEach { p ->
                    val postrec = skill.postreqs.firstOrNull { it.name == p }
                    assertNotNull(postrec)
                    assertEquals(postrec!!.name, p)
                }
            } else {
                assertEmpty(skill.postreqs)
            }

            assertEquals(skill.category.name, categoryName)

        }
    }

    // Full Character Modified Skill Model
    @Test
    fun testFullCharacterModifiedSkillModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val davis = DataManager.shared.getActiveCharacter()
            assertNotNull(davis)
            val allSkills = davis!!.allSkillsWithCharacterModifications()
            val allSkillsBase = DataManager.shared.skills

            allSkills.forEach { skill ->
                val fs = allSkillsBase.firstOrNull { it.id == skill.id }
                assertNotNull(fs)

                assertEquals(skill.name, fs!!.name)
                assertEquals(skill.skillTypeId, fs.skillTypeId)
                assertEquals(skill.description, fs.description)
                assertEquals(skill.category.id, fs.category.id)
                assertEquals(skill.prestigeCost(), fs.prestigeCost)
                assertEquals(skill.baseXpCost(), fs.xpCost)
                assertEquals(skill.baseInfectionCost(), fs.minInfection)
            }
        }
    }

    @Test
    fun testFullCharacterModifiedSkillModelUtils() = runTest {
        loadDataManagerHappyPath(this) {
            val davis = DataManager.shared.getActiveCharacter()
            assertNotNull(davis)
            val purchasedSkills = davis!!.allPurchasedSkills()
            val unpurchasedSkills = davis.allNonPurchasedSkills()
            val allSkillsNoContext = DataManager.shared.getSkillsAsFCMSM()

            // Commander Davis has taken 3 new skills since the first event
            assertCount(purchasedSkills.filter { it.isNew(DataManager.shared.events.first()) }, 3)

            purchasedSkills.forEach { skill ->
                val noContextSkill = allSkillsNoContext.firstOrNull { it.id == skill.id }
                assertNotNull(noContextSkill)

                val costMod = skill.getRelevantSpecCostChange()

                // Commander Davis has a discount on combat and profession skills but a penalty to talent skills
                assertEquals(costMod, (skill.skillTypeId.equalsAnyOf(listOf(Constants.SkillTypes.combat, Constants.SkillTypes.profession))).ternary(-1, 1))
                // The same skill from base with no context should have 0 modifications to costs:
                assertEquals(noContextSkill!!.getRelevantSpecCostChange(), 0)

                if (skill.baseXpCost() > 0) {
                    assertTrue(skill.spentXp() > 0 || skill.spentFt1s() > 0)
                }
                if (skill.prestigeCost() > 0) {
                    assertTrue(skill.spentPp() > 0)
                }

                assertEquals(skill.getTypeText(), noContextSkill.getTypeText())
                assertEquals(skill.hasPrereqs(), noContextSkill.hasPrereqs())
                assertEquals(skill.getPrereqNames(), noContextSkill.getPrereqNames())

                assertTrue(skill.isPurchased())
                assertFalse(noContextSkill.isPurchased())

                val costText = skill.getFullCostText(true)

                assertTrue(costText.startsWith("Already Purchased With"))
                if (skill.hasModCost()) {
                    assertTrue(costText.contains("changed from ${skill.baseXpCost()}xp with"))
                }
                if (skill.spentFt1s() > 0) {
                    assertTrue(costText.contains("Free Tier 1 Skill"))
                }
                if (skill.usesInfection()) {
                    assertTrue(costText.contains("Inf Threshold"))
                }
                if (skill.usesPrestige()) {
                    assertTrue(costText.contains("pp"))
                }
            }
            unpurchasedSkills.forEach { skill ->
                val noContextSkill = allSkillsNoContext.firstOrNull { it.id == skill.id }
                assertNotNull(noContextSkill)

                val costMod = skill.getRelevantSpecCostChange()
                assertEquals(skill.modXpCost(), noContextSkill!!.baseXpCost() - costMod)

                if (skill.hasModCost()) {
                    assertNotEquals(skill.modXpCost(), skill.baseXpCost())
                    assertNotEquals(skill.modXpCost(), noContextSkill.baseXpCost())
                    assertNotEquals(skill.baseXpCost(), noContextSkill.baseXpCost())
                }

                assertFalse(skill.isPurchased())

                val costText = skill.getFullCostText(true)
                if (skill.canUseFreeSkill()) {
                    assertTrue(costText.contains("1 Free Tier-1 Skill"))
                } else {
                    assertTrue(costText.contains("${skill.modXpCost()}xp"))
                }

                if (skill.hasModCost()) {
                    assertTrue(costText.contains("changed from ${skill.baseXpCost()} with"))
                }

                // Commander Davis has a special class xp reduction for Interrogator
                if (skill.id == Constants.SpecificSkillIds.interrogator) {
                    assertTrue(costText.contains("-1 from Special Class Xp Reductions"))
                }
            }
        }
    }

    // Sub Models

    // Character Skill Model and Xp Reduction Model (isNew, purchaseDate, spentXp, spentFt1s, spentPp, hasXpReduction)

    // Skill Filter/search

    companion object {

        @JvmStatic
        fun skillProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(
                globalFromJson<SkillModel>("{\"description\":\"As Fortunate Find but, instead of a Game Runner rolling randomly, you can choose which material you want. If you pick Wood, Cloth, or Stone, you gain 2d4 of that material. If you pick Tech Supplies or Medical Supplies, you gain 2 of that material.\\n\\n*This is a prestige skill. It requires 1 prestige point in addition to its xp cost.\",\"id\":98,\"minInfection\":\"0\",\"name\":\"Prosperous Discovery\",\"prestigeCost\":\"1\",\"skillCategoryId\":\"14\",\"skillTypeId\":\"3\",\"xpCost\":\"4\"}"),
                listOf<String>("Fortunate Find"),
                listOf<String>(),
                "Prestige"
            ),
            Arguments.of(
                globalFromJson<SkillModel>("{\"description\":\"The infection in your body has hardened your skin. At check in you get a set of Red Beads function like Bullet-Proof Armor (works like Standard Armor but also protects against Gunshots, Rockets, and Gusher Balls).\",\"id\":70,\"minInfection\":\"50\",\"name\":\"Scaled Skin\",\"prestigeCost\":\"0\",\"skillCategoryId\":\"13\",\"skillTypeId\":\"3\",\"xpCost\":\"2\"}"),
                listOf<String>(),
                listOf<String>("Shifting Vitals"),
                "The Infected"
            ),
            Arguments.of(
                globalFromJson<SkillModel>("{\"description\":\"As Light Firearm Dual Wielding and Light Melee Weapon Dual Wielding, but you are able to wield a Light Melee Weapon in one hand and a Light Firearm in the other.\",\"id\":83,\"minInfection\":\"0\",\"name\":\"True Light Dual Wielding\",\"prestigeCost\":\"0\",\"skillCategoryId\":\"3\",\"skillTypeId\":\"1\",\"xpCost\":\"2\"}"),
                listOf<String>("Light Firearm Dual Wielding", "Light Melee Weapon Dual Wielding"),
                listOf<String>("True Medium Dual Wielding"),
                "Dual Wielding"
            ),
            Arguments.of(
                globalFromJson<SkillModel>("{\"description\":\"You are skilled at throwing weapons. You may throw any Super Light Melee Weapon. On a successful hit, it deals damage as a regular melee strike. You are free to retrieve your thrown weapon as normal. \\r\\n\\r\\nAdditionally, you have the ability to throw Grenades (Rockets) by shouting \\\"Grenade!\\\".\\r\\n\\r\\nFinally, if you also possess the Ranged Tap skill, you can use it with a thrown Super Light Melee Weapon.\",\"id\":109,\"minInfection\":\"0\",\"name\":\"Thrown Weapons\",\"prestigeCost\":\"0\",\"skillCategoryId\":\"6\",\"skillTypeId\":\"1\",\"xpCost\":\"2\"}"),
                listOf<String>(),
                listOf<String>(),
                "Combat Techniques"
            ),
            Arguments.of(
                globalFromJson<SkillModel>("{\"description\":\"In order to finish off any Helpless zombie or human, you must Tap them. To do so, you put a weapon (that youâ€™re proficient with) up to their torso and roleplay finishing them off while saying â€œTAPâ€�. If youâ€™re using a gun, tapping doesnâ€™t require a bullet (do not shoot them from point blank, just roleplay it).\",\"id\":76,\"minInfection\":\"0\",\"name\":\"Tap\",\"prestigeCost\":\"0\",\"skillCategoryId\":\"1\",\"skillTypeId\":\"1\",\"xpCost\":\"0\"}"),
                listOf<String>(),
                listOf<String>(),
                "Beginner (Free) Skills"
            ),
        )

    }

}