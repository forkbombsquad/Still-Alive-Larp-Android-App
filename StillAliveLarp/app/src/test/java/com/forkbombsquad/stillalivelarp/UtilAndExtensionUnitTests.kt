package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.models.SkillPrereqModel
import com.forkbombsquad.stillalivelarp.utils.AwardCharType
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.AwardType
import com.forkbombsquad.stillalivelarp.utils.BaseUiTestClass
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.addCreateListIfNecessary
import com.forkbombsquad.stillalivelarp.utils.addMinOne
import com.forkbombsquad.stillalivelarp.utils.capitalizeOnlyFirstLetterOfEachWord
import com.forkbombsquad.stillalivelarp.utils.capitalized
import com.forkbombsquad.stillalivelarp.utils.containsIgnoreCase
import com.forkbombsquad.stillalivelarp.utils.doesNotContain
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.equalsIgnoreCase
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.replaceHtmlTag
import com.forkbombsquad.stillalivelarp.utils.replaceHtmlTagWithTag
import com.forkbombsquad.stillalivelarp.utils.replaceHtmlTagWithTagAndInnerValue
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.tryOptional
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddtoDate
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

class UtilAndExtensionUnitTests: BaseUiTestClass {

    override fun setDataLoadType(): BaseUiTestClass.DataLoadType {
        return BaseUiTestClass.DataLoadType.NONE
    }

    // AwardEnums
    @ParameterizedTest(name = "AwardType {0}, pluralized: {1}, should display: {2}")
    @MethodSource("awardTypeProvider")
    fun testAwardDisplayText(awardType: AwardType, pluralized: Boolean, expected: String) {
        // Given an award type
        // When display text is generated
        // Then it should equal the expected text, even when plural
        assertEquals(awardType.getDisplayText(pluralized), expected)
    }

    // Boolean Extensions
    @Test
    fun testBooleanExtensions() {
        // Given a true and false boolean
        val btrue = true
        val bfalse = false
        // When using the added extensions for string, int, bool, and generic T
        // Then they should function properly
        assertEquals(btrue.ternary("value", "otherwise"), "value")
        assertEquals(bfalse.ternary("value", "otherwise"), "otherwise")
        assertEquals(btrue.ternary(1, 0), 1)
        assertEquals(bfalse.ternary(1, 0), 0)
        assertEquals(btrue.ternary(value = false, otherwise = true), false)
        assertEquals(bfalse.ternary(value = false, otherwise = true), true)

        val referenceTypeListOne = listOf(10, 20, 30)
        val referenceTypeListTwo = listOf(5, 10, 15)

        assertEquals(btrue.ternary(referenceTypeListOne, referenceTypeListTwo), referenceTypeListOne)
        assertEquals(bfalse.ternary(referenceTypeListOne, referenceTypeListTwo), referenceTypeListTwo)
    }

    // CanvasAndShape Extensions skipped because of jvm stuff
    // Date extensions test
    @Test
    fun testDateExtensions() {
        val originalDateString = "2025/08/05"
        val date = LocalDate.parse(originalDateString, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        assertEquals(date.yyyyMMddFormatted(), originalDateString)
    }

    // Global function tests (but only the non ui ones)
    @Test
    fun testGlobalFunctions() {
        // To JSON
        val skillPrereq = SkillPrereqModel(1, 10, 20)
        val json = globalToJson(skillPrereq)
        assertEquals(json, "{\"id\":1,\"baseSkillId\":10,\"prereqSkillId\":20}")

        // From JSON
        val prereqFromOne: SkillPrereqModel? = globalFromJson(json)
        val type = object : TypeToken<SkillPrereqModel>() {}.type
        val prereqFromTwo: SkillPrereqModel? = globalFromJson(json, type)
        assertNotNull(prereqFromOne)
        assertNotNull(prereqFromTwo)
        assertEquals(skillPrereq.id, prereqFromOne!!.id)
        assertEquals(skillPrereq.id, prereqFromTwo!!.id)
        assertEquals(skillPrereq.baseSkillId, prereqFromOne.baseSkillId)
        assertEquals(skillPrereq.baseSkillId, prereqFromTwo.baseSkillId)
        assertEquals(skillPrereq.prereqSkillId, prereqFromOne.prereqSkillId)
        assertEquals(skillPrereq.prereqSkillId, prereqFromTwo.prereqSkillId)

        // Try Optional
        val base = 16
        var comparison = 15
        val positiveResult: String? = tryOptional {
            if (base > comparison) {
                "It worked"
            } else {
                throw Exception()
            }
        }
        comparison = 18
        val negativeResult = tryOptional {
            if (base > comparison) {
                "It worked"
            } else {
                throw Exception()
            }
        }
        assertEquals(positiveResult, "It worked")
        assertNull(negativeResult)
    }

    // Int Extensions
    @Test
    fun testIntExtensions() {
        // Add Min 1
        var testValue = 10
        testValue = testValue.addMinOne(10)
        assertEquals(testValue, 20)

        testValue = testValue.addMinOne(-200)
        assertEquals(testValue, 1)

        // EqualsAnyOf
        assertTrue(10.equalsAnyOf(listOf(5, 15, 10, 12)))
        assertFalse(10.equalsAnyOf(listOf(5, 15, 1, 12)))
        assertTrue(10.equalsAnyOf(arrayOf(5, 15, 10, 12)))
        assertFalse(10.equalsAnyOf(arrayOf(5, 15, 1, 12)))
        assertTrue(10.equalsAnyOf(intArrayOf(5, 15, 10, 12)))
        assertFalse(10.equalsAnyOf(intArrayOf(5, 15, 1, 12)))

        // Sort Skills
        // TODO need to test several functions here this once the complex models are set up
    }

    // List Extensions
    @Test
    fun testListExtensions() {
        // Does not contain
        val starterList = listOf("one", "two", "three")
        assertTrue(starterList.doesNotContain("four"))
        assertFalse(starterList.doesNotContain("three"))

        assertTrue(starterList.doesNotContain(listOf("four", "five", "six")))
        assertFalse(starterList.doesNotContain(listOf("one", "two", "three")))
        assertFalse(starterList.doesNotContain(listOf("four", "two", "five")))
    }

    // Test map extensions
    @Test
    fun testMapExtensions() {
        val initalMap: MutableMap<String, MutableList<String>> = mutableMapOf(Pair("key", mutableListOf("value1")))
        val initalEmptyMap: MutableMap<String, MutableList<String>> = mutableMapOf()
        val value2 = "value2"
        assertFalse(initalMap["key"]?.contains(value2) ?: true)
        assertFalse(initalEmptyMap["key"]?.contains(value2) ?: false)

        initalMap.addCreateListIfNecessary("key", value2)
        initalEmptyMap.addCreateListIfNecessary("key", value2)

        assertTrue(initalMap["key"]?.contains(value2) ?: false)
        assertTrue(initalEmptyMap["key"]?.contains(value2) ?: false)
        assertFalse(initalEmptyMap["key"]?.contains("value1") ?: true)

    }

    // Optional Extensions
    @Test
    fun testOptionalExtensions() {
        val nullValue: String? = null
        val notNullValue: String? = "Ope"

        var isNull = true

        nullValue.ifLet {
            isNull = false
        }
        assertTrue(isNull)

        isNull = false

        nullValue.ifLet({
            isNull = false
        }, {
            isNull = true
        })
        assertTrue(isNull)

        isNull = true
        notNullValue.ifLet {
            isNull = false
        }
        assertFalse(isNull)

        isNull = true
        notNullValue.ifLet({
            isNull = false
        }, {
            isNull = true
        })
        assertFalse(isNull)
    }

    // TODO eventually test rulebook

    @Test
    fun testStringExtensions() {
        // Capitalized
        val lowercaseString = "hello my friend"
        val uppercaseString = "HI MY FRIEND"
        assertEquals(lowercaseString.capitalized(), "Hello my friend")
        assertEquals(uppercaseString.capitalized(), uppercaseString)

        // yyyyMMddtoDate
        val originalDateString = "2025/08/05"
        val date = LocalDate.parse(originalDateString, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val strDate = originalDateString.yyyyMMddtoDate()
        assertEquals(date, strDate)

        // yyyyMMddToMonthDayYear
        assertEquals(originalDateString.yyyyMMddToMonthDayYear(), "August 05, 2025")

        // containsIgnoreCase
        val baseString = "Hello my name is Rydge"
        val lowercaseContains = "hello my"

        assertFalse(baseString.contains(lowercaseContains))
        assertTrue(baseString.containsIgnoreCase(lowercaseContains))

        // Replace Html Tags
        val htmlString = "<strong><a><b><br>test</b></a></strong>"
        assertEquals(htmlString.replaceHtmlTag("strong").replaceHtmlTag("br", "\n"), "<a><b>\ntest</b></a>")
        assertEquals(htmlString.replaceHtmlTagWithTag("strong", "weak").replaceHtmlTagWithTag("b", "c"), "<weak><a><c><br>test</c></a></weak>")
        assertEquals(htmlString.replaceHtmlTagWithTagAndInnerValue("a", "a", "href=\"https://www.example.com\""), "<strong><a href=\"https://www.example.com\"><b><br>test</b></a></strong>")

        // Compress & Decompress not tested because Base64 is not mocked
        // equalsIgnoreCase
        val casedString = "ThIs Is A StRiNg"
        val lowercasedString = "this is a string"
        assertFalse(casedString == lowercasedString)
        assertTrue(casedString.equalsIgnoreCase(lowercasedString))

        // Cap first of each word
        val title = "this is a title or a name"
        assertEquals(title.capitalizeOnlyFirstLetterOfEachWord(), "This Is A Title Or A Name")
    }

    @ParameterizedTest(name = "ValidationType {0}, textToValidate: {1}, errors: {2}")
    @MethodSource("validationTypeProvider")
    fun testValidator(validationType: ValidationType, textToValidate: String, errors: String?) {
        assertEquals(Validator.doValidation(textToValidate, validationType), errors)
    }

    companion object {

        @JvmStatic
        fun awardTypeProvider(): Stream<org.junit.jupiter.params.provider.Arguments> = Stream.of(
            org.junit.jupiter.params.provider.Arguments.of(AwardPlayerType.XP, true, "Experience Points"),
            org.junit.jupiter.params.provider.Arguments.of(AwardPlayerType.XP, false, "Experience Point"),
            org.junit.jupiter.params.provider.Arguments.of(AwardPlayerType.PRESTIGEPOINTS, true, "Prestige Points"),
            org.junit.jupiter.params.provider.Arguments.of(AwardPlayerType.PRESTIGEPOINTS, false, "Prestige Point"),
            org.junit.jupiter.params.provider.Arguments.of(AwardPlayerType.FREETIER1SKILLS, true, "Free Tier-1 Skills"),
            org.junit.jupiter.params.provider.Arguments.of(AwardPlayerType.FREETIER1SKILLS, false, "Free Tier-1 Skill"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.INFECTION, true, "Infection Rating"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.INFECTION, false, "Infection Rating"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALCASINGS, true, "Bullet Casings"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALCASINGS, false, "Bullet Casing"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALWOOD, true, "Wood"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALWOOD, false, "Wood"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALCLOTH, true, "Cloth"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALCLOTH, false, "Cloth"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALMETAL, true, "Metal"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALMETAL, false, "Metal"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALTECH, true, "Tech Supplies"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALTECH, false, "Tech Supply"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALMED, true, "Medical Supplies"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.MATERIALMED, false, "Medical Supply"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMOBULLET, true, "Bullets"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMOBULLET, false, "Bullet"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMOMEGA, true, "Megas"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMOMEGA, false, "Mega"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMORIVAL, true, "Rivals"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMORIVAL, false, "Rival"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMOROCKET, true, "Rockets"),
            org.junit.jupiter.params.provider.Arguments.of(AwardCharType.AMMOROCKET, false, "Rocket")
        )

        @JvmStatic
        fun validationTypeProvider(): Stream<org.junit.jupiter.params.provider.Arguments> = Stream.of(
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.FULL_NAME, "John Doe", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.FULL_NAME, "John Jacob Jingle Heimer Schimidt", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.FULL_NAME, "", "Full name must not be empty\nFull name must be at least 5 characters long\nFull name must contain a space"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.EMAIL, "example@example.com", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.EMAIL, "", "Email must not be empty\nEmail must contain @\nEmail must contain .\nEmail must be at least 8 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.PASSWORD, "SomeCoolPassword", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.PASSWORD, "", "Password must not be empty\nPassword must be at least 8 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.SECURITY_CODE, "securityCode", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.SECURITY_CODE, "", "Security code must not be empty"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.POSTAL_CODE, "54703", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.POSTAL_CODE, "Y1A 0A1", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.POSTAL_CODE, "", "Postal code must not be empty\nPostal code must be between 5 and 7 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.MESSAGE, "message", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.MESSAGE, "", "Message must not be empty"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.INFECTION, "50", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.INFECTION, "", "Infection must consist of only numbers!\nInfection must be between 0 and 100"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.BULLETS, "1", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.BULLETS, "", "Bullets must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.MEGAS, "99", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.MEGAS, "", "Megas must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.RIVALS, "800", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.RIVALS, "", "Rivals must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.ROCKETS, "6000", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.ROCKETS, "", "Rockets must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.BULLET_CASINGS, "-1", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.BULLET_CASINGS, "", "Bullet casings must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.CLOTH, "-999", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.CLOTH, "", "Cloth must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.WOOD, "6", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.WOOD, "", "Wood must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.METAL, "18", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.METAL, "", "Metal must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.TECH, "4", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.TECH, "", "Tech must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.MEDICAL, "77", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.MEDICAL, "", "Medical must consist of only numbers!"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.ANNOUNCEMENT_TITLE, "An Announcement", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.ANNOUNCEMENT_TITLE, "", "Announcement title must not be empty\nAnnouncement title must be at least 5 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.ANNOUNCEMENT_MESSAGE, "A message for the announcement", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.ANNOUNCEMENT_MESSAGE, "", "Announcement message must not be empty\nAnnouncement message must be at least 5 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.INTRIGUE, "Mysterious", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.INTRIGUE, "", "Intrigue must not be empty"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.TITLE, "My Title", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.TITLE, "", "Title must not be empty\nTitle must be at least 5 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.DATE, "2025/08/05", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.DATE, "08/05/2025", "Date must be formatted exactly as yyyy/MM/dd, i.e. 2023/23/01"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.DATE, "2025-08-05", "Date must be formatted exactly as yyyy/MM/dd, i.e. 2023/23/01"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.DATE, "", "Date must not be empty\nDate must be exactly 10 characters long\nDate must be formatted exactly as yyyy/MM/dd, i.e. 2023/23/01"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.START_TIME, "10:00am", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.START_TIME, "", "Start time must not be empty"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.END_TIME, "8:00pm", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.END_TIME, "", "End time must not be empty"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.DESCRIPTION, "A description", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.DESCRIPTION, "", "Description must not be empty"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.GEAR_NAME, "A Gear Name", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.GEAR_NAME, "", "Gear name must not be empty\nGear name must be at least 2 characters long"),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.GEAR_DESCRIPTION, "A Gear Desc", null),
            org.junit.jupiter.params.provider.Arguments.of(ValidationType.GEAR_DESCRIPTION, "", "Gear description must not be empty\nGear description must be at least 2 characters long")
        )

    }
}