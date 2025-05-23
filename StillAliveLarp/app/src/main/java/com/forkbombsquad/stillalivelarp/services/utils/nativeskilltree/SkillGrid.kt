package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.services.models.CharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.OldFullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.Shapes
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sign

class SkillGrid(skills: List<OldFullSkillModel>, skillCategories: List<SkillCategoryModel>, personal: Boolean = false, allowPurchase: Boolean = false) {
    private val personal: Boolean
    private val allowPurchase: Boolean

    private val skills: List<OldFullSkillModel>
    private var purchaseableSkills: List<CharacterModifiedSkillModel> = listOf()
    private val skillCategories: List<SkillCategoryModel>
    private val gridCategories: MutableList<SkillGridCategory> = mutableListOf()
    private var trueGrid: List<GridSkill> = listOf()

    private var purchaseButton: TappablePurchaseButton? = null

    private val skillWidth = 300f
    private val skillWidthExpanded = 600f
    private val skillHeight = 300f
    private val spacingWidth = 75f
    private val spacingHeight = 150f
    private val lineHeight = spacingHeight / 2f
    private val fullTitleSize = 100f
    private val textSize = 35f
    private val titleSize = 60f
    private val titleSpacing = 20f
    private val numberCircleRadius = 50f
    private val textPadding = 4

    private val skillReqSpacing = 25

    // Dotted Lines
    private var firstLineYOffset = 0f
    private var secondLineYOffset = 0f
    private var thirdLineYOffset = 0f
    private var lineStartXOffset = 0f
    private var lineEndXOffset = 0f

    // XP Cost Column
    private val xpCostWidth = skillWidth + spacingWidth + spacingWidth
    private val diamondWidth = 300f
    private val diamondHeight = 300f

    private val buttonOutlineHeight = 8f

    private val paint = Paint()
    private val blackPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10F
        textSize = 50F

    }
    private val outline = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5F
    }
    private val blackFill = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 5F
    }
    private val fullTitlePaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = this@SkillGrid.fullTitleSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val textPaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = this@SkillGrid.textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val textPaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = this@SkillGrid.textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val skillDetailTitlePaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 40f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailTitlePaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = 40f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val skillRequirementPaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailSkillTypePaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 28f
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailSkillTypePaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = 28f
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailCostPaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailCostPaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailPrereqTitlePaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailPrereqTitlePaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = 35f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailPrereqPaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailPrereqPaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailDescPaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailDescPaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }

    private val titlePaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = titleSize
        textAlign = Paint.Align.CENTER
    }
    private val dottedLine = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5F
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    private val linePaint = Paint().apply {
        color = Color.parseColor("#DDDDDD")
        strokeWidth = 10F
        style = Paint.Style.STROKE
    }
    private val dividingLinePaint = Paint().apply {
        color = Color.parseColor("#222222")
        strokeWidth = 4F
        style = Paint.Style.STROKE
    }
    private val circlePaint = Paint().apply {
        isDither = true
        style = Paint.Style.FILL
        strokeWidth = 1F
    }

    private val purchaseButtonPaint = TextPaint().apply {
        color = Color.parseColor("#910016")
        style = Paint.Style.FILL
        strokeWidth = 4F
        textAlign = Paint.Align.CENTER
    }
    private val purchaseButtonPaintOutline = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = buttonOutlineHeight
        textAlign = Paint.Align.CENTER
    }
    private val buttonTextPaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 70F
        textAlign = Paint.Align.CENTER
    }

    private var makingPurchase = false

    private var dotCount = 0
    private var baseText = "Purchasing"
    private val dotHandler = Handler(Looper.getMainLooper())
    private val dotRunnable = object : Runnable {
        override fun run() {
            if (makingPurchase) {
                dotCount = (dotCount + 1) % 4 // cycle 0–3
                invalidate() // trigger redraw
                dotHandler.postDelayed(this, 500) // update every 500ms
            }
        }
    }

    // ARGB
    private val lightGray = Color.parseColor("#DDDDDD")
    private val darkGray = Color.parseColor("#999999")
    private val lightGrayDull = Color.parseColor("#797979")
    private val darkGrayDull = Color.parseColor("#353535")

    private val lightRed = Color.parseColor("#F7C9C6")
    private val darkRed = Color.parseColor("#EA6E69")
    private val lightRedDull = Color.parseColor("#936562")
    private val darkRedDull = Color.parseColor("#860A05")

    private val lightBlue = Color.parseColor("#D8E7FB")
    private val darkBlue = Color.parseColor("#7FA7E0")
    private val lightBlueDull = Color.parseColor("#748397")
    private val darkBlueDull = Color.parseColor("#1B437C")

    private val lightGreen = Color.parseColor("#CAE1C5")
    private val darkGreen = Color.parseColor("#98D078")
    private val lightGreenDull = Color.parseColor("#667D61")
    private val darkGreenDull = Color.parseColor("#346C14")

    val fullGrid: MutableList<MutableList<OldFullSkillModel?>>
    val gridConnections: List<GridConnection>

    lateinit var invalidate: () -> Unit

    init {
        this.skills = skills
        this.skillCategories = skillCategories
        this.personal = personal
        this.allowPurchase = allowPurchase

        calculateWidthAndHeightOfGridCategories()
        orderCategories()
        fullGrid = calculateFullGrid()
        gridConnections = buildConnections()
        trueGrid = calculateTrueGrid()

        if (personal && allowPurchase) {
            purchaseableSkills = getAvailableSkills(skills, OldDataManager.shared.selectedPlayer, OldDataManager.shared.charForSelectedPlayer, OldDataManager.shared.xpReductions)
        }
    }

    private fun getAvailableSkills(skls: List<OldFullSkillModel>?, player: PlayerModel?, character: FullCharacterModel?, xpReductions: List<XpReductionModel>?): List<CharacterModifiedSkillModel> {
        val allSkills = skls ?: listOf()
        val charSkills: List<OldFullSkillModel> = character?.skills?.toList() ?: listOf()

        // Remove skills the character already has
        var newSkillList: List<OldFullSkillModel> = allSkills.filter { skillToKeep ->
            charSkills.firstOrNull { charSkill ->
                charSkill.id == skillToKeep.id
            } == null
        }

        // Remove all skills you don't have prereqs for
        newSkillList = newSkillList.filter { skillToKeep ->
            if (skillToKeep.prereqs.isEmpty()) {
                true
            } else {
                var keep = true
                for (prereq in skillToKeep.prereqs) {
                    if (charSkills.firstOrNull { charSkill ->
                            charSkill.id == prereq.id
                        } == null) {
                        keep = false
                        break
                    }
                }
                keep
            }
        }

        // Filter out pp skills you don't qualify for
        newSkillList = newSkillList.filter { skillToKeep ->
            skillToKeep.prestigeCost.toInt() <= (player?.prestigePoints?.toInt() ?: 0)
        }

        // Remove Choose One Skills that can't be chosen
        val cskills: List<OldFullSkillModel> = character?.getChooseOneSkills()?.toList() ?: listOf()
        if (cskills.isEmpty()) {
            // Remove all level 2 cskills
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(Constants.SpecificSkillIds.allLevel2SpecialistSkills)
            }
        } else if (cskills.count() == 2) {
            // Remove all cskills
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(Constants.SpecificSkillIds.allSpecalistSkills)
            }
        } else if (cskills.firstOrNull() != null) {
            val cskill = cskills.first()
            var idsToRemove: Array<Int> = arrayOf()
            when (cskill.id) {
                Constants.SpecificSkillIds.expertCombat -> idsToRemove =
                    Constants.SpecificSkillIds.allSpecalistsNotUnderExpertCombat

                Constants.SpecificSkillIds.expertProfession -> idsToRemove =
                    Constants.SpecificSkillIds.allSpecalistsNotUnderExpertProfession

                Constants.SpecificSkillIds.expertTalent -> idsToRemove =
                    Constants.SpecificSkillIds.allSpecalistsNotUnderExpertTalent
            }
            // Remove cskills not under your exper skill
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(idsToRemove)
            }
        }

        val combatXpMod = character?.costOfCombatSkills() ?: 0
        val professionXpMod = character?.costOfProfessionSkills() ?: 0
        val talentXpMod = character?.costOfTalentSkills() ?: 0
        val inf50Mod = character?.costOf50InfectSkills() ?: 50
        val inf75Mod = character?.costOf75InfectSkills() ?: 75

        // Convert to new model type
        var newCharModSkills: MutableList<CharacterModifiedSkillModel> = mutableListOf()
        newSkillList.forEach { skill ->
            newCharModSkills.add(
                CharacterModifiedSkillModel(
                    fsm = skill,
                    modXpCost = skill.getModCost(
                        combatMod = combatXpMod,
                        professionMod = professionXpMod,
                        talentMod = talentXpMod,
                        xpReductions = xpReductions?.toTypedArray() ?: arrayOf()
                    ).toString(),
                    modInfCost = skill.getInfModCost(inf50Mod, inf75Mod).toString()
                )
            )
        }

        // Filter out skills that you don't have enough xp, fs, or inf for
        return newCharModSkills.filter { skillToKeep ->
            var keep = true
            keep = if (skillToKeep.modInfCost.toInt() > (character?.infection?.toInt() ?: 0)) {
                false
            } else if (skillToKeep.modXpCost.toInt() > (player?.experience?.toInt() ?: 0)) {
                skillToKeep.canUseFreeSkill() && (player?.freeTier1Skills?.toInt() ?: 0) > 0
            } else {
                true
            }
            keep
        }
    }

    fun getSkillColor(x: Float, y: Float, skill: OldFullSkillModel): Paint {
        var gradient: LinearGradient? = null
        when (skill.skillTypeId.toInt()) {
            Constants.SkillTypes.combat -> {
                if ((personal && OldDataManager.shared.charForSelectedPlayer?.skills?.firstOrNull { it.id == skill.id } != null) || !personal) {
                    // Normal Color
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightRed, darkRed, Shader.TileMode.CLAMP)
                } else if (couldPurchaseSkill(skill)) {
                    // Dull Color
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightRedDull, darkRedDull, Shader.TileMode.CLAMP)
                } else if  (personal) {
                    // Grayscale
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightGrayDull, darkGrayDull, Shader.TileMode.CLAMP)
                }
            }
            Constants.SkillTypes.profession -> {
                if ((personal && OldDataManager.shared.charForSelectedPlayer?.skills?.firstOrNull { it.id == skill.id } != null) || !personal) {
                    // Normal Color
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightGreen, darkGreen, Shader.TileMode.CLAMP)
                } else if (couldPurchaseSkill(skill)) {
                    // Dull Color
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightGreenDull, darkGreenDull, Shader.TileMode.CLAMP)
                } else if  (personal) {
                    // Grayscale
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightGrayDull, darkGrayDull, Shader.TileMode.CLAMP)
                }
            }
            Constants.SkillTypes.talent -> {
                if ((personal && OldDataManager.shared.charForSelectedPlayer?.skills?.firstOrNull { it.id == skill.id } != null) || !personal) {
                    // Normal Color
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightBlue, darkBlue, Shader.TileMode.CLAMP)
                } else if (couldPurchaseSkill(skill)) {
                    // Dull Color
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightBlueDull, darkBlueDull, Shader.TileMode.CLAMP)
                } else if  (personal) {
                    // Grayscale
                    gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightGrayDull, darkGrayDull, Shader.TileMode.CLAMP)
                }
            }
        }
        paint.isDither = true
        paint.shader = gradient
        return paint
    }

    fun getSkillConnectionColor(typeId: Int): Int {
        var gradient: LinearGradient? = null
        when (typeId) {
            Constants.SkillTypes.combat -> {
                return darkRed
            }
            Constants.SkillTypes.profession -> {
                return darkGreen
            }
            Constants.SkillTypes.talent -> {
                return darkBlue
            }
        }
        return blackPaint.color
    }
    private fun couldPurchaseSkill(skill: OldFullSkillModel): Boolean {
        if (personal && allowPurchase) {
            return purchaseableSkills.firstOrNull { it.id == skill.id } != null
        }
        return false
    }



    fun draw(canvas: Canvas, scaleFactor: Float) {
        purchaseButton = null
        // Draw Category Boxes
        val exSkill = getExapnded()
        var widthSoFar = 0f
        var startDottedLinesX = 0f
        var finalDottedLineX = 0f

        var skillRequirements: MutableList<SkillRequirement> = mutableListOf()

        gridCategories.forEachIndexed { index, skillGridCategory ->
            // Outline Category and Name it
            var extraXoffset = 0f
            var extraWidth = 0f
            var extraHeight = 0f
            exSkill.ifLet {
                if (it.skill.skillCategoryId.toInt() - 1 < index) {
                    extraXoffset = skillWidthExpanded - skillWidth
                }
                if (it.skill.skillCategoryId.toInt() - 1 == index) {
                    extraWidth = skillWidthExpanded - skillWidth
                }
                extraHeight = it.rect.height() - skillHeight
            }
            canvas.drawRect(Shapes.rectf(widthSoFar + extraXoffset, 0f, skillGridCategory.width * skillWidth + extraWidth + (skillGridCategory.width * 2 * spacingWidth), 8 * skillHeight + (spacingHeight * 16) + extraHeight), outline)
            val titleLayout = StaticLayout.Builder.obtain(skillGridCategory.skillCategoryName, 0, skillGridCategory.skillCategoryName.length, titlePaint, skillGridCategory.width * 2 * skillWidth.toInt() + extraWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .setIncludePad(false)
                .build()
            canvas.save()
            canvas.translate(widthSoFar + extraXoffset + ((skillGridCategory.width * skillWidth + (spacingWidth * skillGridCategory.width * 2)) / 2f), titleSize)
            titleLayout.draw(canvas)
            canvas.restore()
            widthSoFar += (skillGridCategory.width * skillWidth) + (skillGridCategory.width * spacingWidth * 2)
            if (skillGridCategory.skillCategoryId == Constants.SpecificSkillCategories.BEGINNER_SKILLS) {
                // Draw Tier xp label column thing
                canvas.drawRect(Shapes.rectf(widthSoFar + extraXoffset + extraWidth, 0f, xpCostWidth, 8 * skillHeight + (spacingHeight * 16) + extraHeight), outline)

                val xpTitle = "Tier - XP Cost"
                val xpTitleLayout = StaticLayout.Builder.obtain(xpTitle, 0, xpTitle.length, titlePaint, skillGridCategory.width * 2 * skillWidth.toInt() + extraWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(widthSoFar + extraXoffset + extraWidth + (xpCostWidth / 2), titleSize)
                xpTitleLayout.draw(canvas)
                canvas.restore()

                val startX = widthSoFar + extraXoffset + extraWidth + spacingWidth
                var startY1 = skillHeight + spacingHeight
                var startY2 = skillHeight + skillHeight + (spacingHeight * 4f) + startY1
                var startY3 = startY2 + skillHeight + skillHeight + (spacingHeight * 4f)
                var startY4 = startY3 + skillHeight + skillHeight + (spacingHeight * 4f)
                exSkill.ifLet { skl ->
                    if (skl.expanded) {
                        val xp = skl.skill.xpCost.toInt()
                        if (xp <= 4) {
                            startY4 += extraHeight
                        }
                        if (xp <= 3) {
                            startY3 += extraHeight
                        }
                        if (xp <= 2) {
                            startY2 += extraHeight
                        }
                        if (xp <= 1) {
                            startY1 += extraHeight
                        }
                    }
                }

                val diamond1 = Path().apply {
                    // Top
                    moveTo(startX + (diamondWidth / 2), startY1)
                    // Right
                    lineTo(startX + diamondWidth, startY1 + (diamondHeight / 2))
                    // Bottom
                    lineTo(startX + (diamondWidth / 2), startY1 + diamondHeight)
                    // Left
                    lineTo(startX, startY1 + (diamondHeight / 2))
                    close()
                }
                val diamond2 = Path().apply {
                    // Top
                    moveTo(startX + (diamondWidth / 2), startY2)
                    // Right
                    lineTo(startX + diamondWidth, startY2 + (diamondHeight / 2))
                    // Bottom
                    lineTo(startX + (diamondWidth / 2), startY2 + diamondHeight)
                    // Left
                    lineTo(startX, startY2 + (diamondHeight / 2))
                    close()
                }
                val diamond3 = Path().apply {
                    // Top
                    moveTo(startX + (diamondWidth / 2), startY3)
                    // Right
                    lineTo(startX + diamondWidth, startY3 + (diamondHeight / 2))
                    // Bottom
                    lineTo(startX + (diamondWidth / 2), startY3 + diamondHeight)
                    // Left
                    lineTo(startX, startY3 + (diamondHeight / 2))
                    close()
                }
                val diamond4 = Path().apply {
                    // Top
                    moveTo(startX + (diamondWidth / 2), startY4)
                    // Right
                    lineTo(startX + diamondWidth, startY4 + (diamondHeight / 2))
                    // Bottom
                    lineTo(startX + (diamondWidth / 2), startY4 + diamondHeight)
                    // Left
                    lineTo(startX, startY4 + (diamondHeight / 2))
                    close()
                }
                canvas.save()
                canvas.drawPath(diamond1, outline)
                canvas.drawPath(diamond2, outline)
                canvas.drawPath(diamond3, outline)
                canvas.drawPath(diamond4, outline)
                canvas.restore()

                var count = 1
                var startY = startY1
                while (count < 5) {
                    val xpLayout = StaticLayout.Builder.obtain(count.toString(), 0, count.toString().length, titlePaint, diamondWidth.toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(1.5f, 1f)
                        .setIncludePad(false)
                        .build()
                    canvas.save()
                    canvas.translate(startX + (diamondWidth / 2), startY + (diamondHeight / 2) - (titleSize / 2))
                    xpLayout.draw(canvas)
                    canvas.restore()
                    count++
                    when (count) {
                        2 -> {
                            startY = startY2
                        }
                        3 -> {
                            startY = startY3
                        }
                        4 -> {
                            startY = startY4
                        }
                    }
                }

                startDottedLinesX = widthSoFar
                widthSoFar += xpCostWidth
            }
            if (index == gridCategories.size - 1) {
                finalDottedLineX = widthSoFar
            }
        }

        // Draw Dotted Lines
        canvas.save()
        dottedLine.pathEffect = DashPathEffect(floatArrayOf(30f, 30f), 0f)
        // Draw Horizontal Lines
        val firstTierLine = Path().apply{
            moveTo(startDottedLinesX + lineStartXOffset, (2f * skillHeight) + (4f * spacingHeight) + firstLineYOffset)
            lineTo(finalDottedLineX + lineEndXOffset, (2f * skillHeight) + (4f * spacingHeight) + firstLineYOffset) // End POint
        }

        val secondTierLine = Path().apply{
            moveTo(startDottedLinesX + lineStartXOffset, (4f * skillHeight) + (8f * spacingHeight) + secondLineYOffset)
            lineTo(finalDottedLineX + lineEndXOffset, (4f * skillHeight) + (8f * spacingHeight) + secondLineYOffset) // End POint
        }

        val thirdTierLine = Path().apply{
            moveTo(startDottedLinesX + lineStartXOffset, (6f * skillHeight) + (12f * spacingHeight) + thirdLineYOffset)
            lineTo(finalDottedLineX + lineEndXOffset, (6f * skillHeight) + (12f * spacingHeight) + thirdLineYOffset) // End POint
        }


        canvas.drawPath(firstTierLine, dottedLine)
        canvas.drawPath(secondTierLine, dottedLine)
        canvas.drawPath(thirdTierLine, dottedLine)
        canvas.restore()

        // Draw Skills
        var x = 0f
        var y = 0f
        this.trueGrid.forEach {
            x = it.rect.left
            y = it.rect.top
            val sectionSpacing = textPadding * 4
            val lineHeight = 4

            if (it.skill.skillCategoryId.toInt() == Constants.SpecificSkillCategories.THE_INFECTED) {
                // Title
                val text = "At Least ${it.skill.minInfection}% Infection Rating Required"
                val layout = StaticLayout.Builder.obtain(text, 0, text.length, skillRequirementPaint, skillWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val titleHeight = layout.height + textPadding + textPadding
                skillRequirements.add(SkillRequirement(Shapes.rectf(x - textPadding, y - skillReqSpacing - titleHeight - textPadding, skillWidth + textPadding, titleHeight.toFloat() + textPadding), layout))
            }

            if (it.skill.skillCategoryId.toInt() == Constants.SpecificSkillCategories.PRESTIGE) {
                // Title
                val text = "Requires 1 Prestige Point"
                val layout = StaticLayout.Builder.obtain(text, 0, text.length, skillRequirementPaint, skillWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val titleHeight = layout.height + textPadding + textPadding
                skillRequirements.add(SkillRequirement(Shapes.rectf(x - textPadding, y - skillReqSpacing - titleHeight - textPadding, skillWidth + textPadding, titleHeight.toFloat() + textPadding), layout))
            }

            if (it.skill.skillCategoryId.toInt() == Constants.SpecificSkillCategories.SPECIALIZATION) {
                // Title
                val text = "You may only select 1 Tier-${it.skill.xpCost} Specialization Skill"

                val layout = StaticLayout.Builder.obtain(text, 0, text.length, skillRequirementPaint, skillWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val titleHeight = layout.height + textPadding + textPadding
                skillRequirements.add(SkillRequirement(Shapes.rectf(x - textPadding, y - skillReqSpacing - titleHeight - textPadding, skillWidth + textPadding, titleHeight.toFloat() + textPadding), layout))
            }

            canvas.drawRect(it.rect, getSkillColor(x, y, it.skill))
            if (it.expanded) {
                var heightSoFar = 0f
                val skill = it.skill
                // Title
                var text = skill.name
                val textWidth = (skillWidthExpanded.toInt() * 0.75).toInt() - (textPadding * 2)

                var outlineLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailTitlePaintOutline, textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                var fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailTitlePaint, textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()

                val titleHeight = outlineLayout.height + textPadding + textPadding
                canvas.save()
                canvas.translate(x + textPadding, y + textPadding)
                outlineLayout.draw(canvas)
                fillLayout.draw(canvas)
                canvas.restore()

                // Type
                text = skill.getTypeText()
                outlineLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailSkillTypePaintOutline, (skillWidthExpanded.toInt() * 0.25).toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailSkillTypePaint, (skillWidthExpanded.toInt() * 0.25).toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val typeHeight = outlineLayout.height + textPadding + textPadding
                canvas.save()
                canvas.translate(x + skillWidthExpanded - textPadding - textPadding, y + textPadding)
                outlineLayout.draw(canvas)
                fillLayout.draw(canvas)
                canvas.restore()

                heightSoFar += max(titleHeight, typeHeight) + sectionSpacing

                canvas.drawLine(x + sectionSpacing, y + heightSoFar, x + skillWidthExpanded - sectionSpacing, y + heightSoFar, dividingLinePaint)
                // Plus dividing line
                heightSoFar += lineHeight + sectionSpacing

                // Cost
                text = skill.getFullCostText(purchaseableSkills)
                outlineLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailCostPaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailCostPaint, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                outlineLayout.draw(canvas)
                fillLayout.draw(canvas)
                canvas.restore()
                heightSoFar += outlineLayout.height + textPadding + textPadding + sectionSpacing

                canvas.drawLine(x + sectionSpacing, y + heightSoFar, x + skillWidthExpanded - sectionSpacing, y + heightSoFar, dividingLinePaint)
                // Plus dividing line
                heightSoFar += lineHeight + sectionSpacing

                if (skill.prereqs.isNotEmpty()) {
                    text = "Prerequisites"
                    outlineLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqTitlePaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(1.5f, 1f)
                        .setIncludePad(false)
                        .build()
                    fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqTitlePaint, skillWidthExpanded.toInt() - (textPadding * 2))
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(1.5f, 1f)
                        .setIncludePad(false)
                        .build()
                    canvas.save()
                    canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                    outlineLayout.draw(canvas)
                    fillLayout.draw(canvas)
                    canvas.restore()
                    heightSoFar += outlineLayout.height + textPadding + textPadding

                    skill.prereqs.forEach { prereq ->
                        text = prereq.name
                        outlineLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqPaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setLineSpacing(1.5f, 1f)
                            .setIncludePad(false)
                            .build()
                        fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqPaint, skillWidthExpanded.toInt() - (textPadding * 2))
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setLineSpacing(1.5f, 1f)
                            .setIncludePad(false)
                            .build()
                        canvas.save()
                        canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                        outlineLayout.draw(canvas)
                        fillLayout.draw(canvas)
                        canvas.restore()
                        heightSoFar += outlineLayout.height + textPadding + textPadding
                    }
                    heightSoFar += sectionSpacing
                    canvas.drawLine(x + sectionSpacing, y + heightSoFar, x + skillWidthExpanded - sectionSpacing, y + heightSoFar, dividingLinePaint)
                    // Plus dividing line
                    heightSoFar += lineHeight + sectionSpacing
                }

                // Desc
                text = skill.description
                outlineLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailDescPaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailDescPaint, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                outlineLayout.draw(canvas)
                fillLayout.draw(canvas)
                canvas.restore()

                heightSoFar += outlineLayout.height + textPadding + textPadding

                // Purchase button
                val pskill = purchaseableSkills.firstOrNull { skl -> skl.id == it.skill.id }
                if (personal && allowPurchase && pskill != null) {
                    // Purchase Skill button
                    text = "Purchase Skill"
                    if (makingPurchase) {
                        val dots = ".".repeat(dotCount)
                        text = "Purchasing$dots"
                    }
                    val purchaseRect = Shapes.rectf(x + (textPadding * 4) , y + (textPadding * 4) + heightSoFar + buttonOutlineHeight, skillWidthExpanded - (textPadding * 8), 100f)
                    canvas.drawRect(purchaseRect, purchaseButtonPaintOutline)
                    canvas.drawRect(purchaseRect, purchaseButtonPaint)
                    fillLayout = StaticLayout.Builder.obtain(text, 0, text.length, buttonTextPaint, skillWidthExpanded.toInt() - (textPadding * 10))
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(1.5f, 1f)
                        .setIncludePad(false)
                        .build()
                    canvas.save()
                    canvas.translate(purchaseRect.centerX(), purchaseRect.top + textPadding + textPadding)
                    fillLayout.draw(canvas)
                    canvas.restore()

                    purchaseButton = TappablePurchaseButton(pskill, purchaseRect)

                    heightSoFar += 100f + (textPadding * 8) + (buttonOutlineHeight * 2)
                }
            } else {
                // Draw the text centered within the box (startX, startY is the top-left position)
                val titleLayout = StaticLayout.Builder.obtain(it.skill.name, 0, it.skill.name.length, textPaint, skillWidth.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val titleOutlineLayout = StaticLayout.Builder.obtain(it.skill.name, 0, it.skill.name.length, textPaintOutline, skillWidth.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val textHeight = titleOutlineLayout.height
                canvas.save()
                canvas.translate(x + (skillWidth / 2f), y + (skillHeight / 2f) - (textHeight / 2f))
                titleOutlineLayout.draw(canvas)
                titleLayout.draw(canvas)
                canvas.restore()
            }
        }

        val circles: MutableMap<Shapes.Circle, Int> = mutableMapOf()

        // Draw Prereq Connections
        gridConnections.forEach { connection ->
            var fx = connection.from.x
            val fy = connection.from.y
            var tx = connection.to.x
            val ty = connection.to.y
            val mult = connection.mult

            x = spacingWidth + (fx * skillWidth) + (fx * spacingWidth * 2) + (skillWidth * mult)
            y = if (!connection.from.isLowered) {
                (fy * skillHeight * 2) + (fy * spacingHeight * 4) + spacingHeight + titleSize + titleSpacing + skillHeight
            } else {
                (fy * skillHeight * 2) + (fy * spacingHeight * 4) + skillHeight + spacingHeight + spacingHeight + titleSize + titleSpacing + skillHeight
            }
            val targetX = spacingWidth + (tx * skillWidth) + (tx * spacingWidth * 2f) + (skillWidth / 2f)
            val targetY = if (!connection.to.isLowered) {
                (ty * skillHeight * 2) + (ty * spacingHeight * 4) + spacingHeight + titleSize + titleSpacing
            } else {
                (ty * skillHeight * 2) + (ty * spacingHeight * 4) + skillHeight + spacingHeight + spacingHeight + titleSize + titleSpacing
            }
            val lineMult = 1f - mult
            var dropVal = skillHeight + spacingHeight
            if (connection.from.isLowered || fy == ty) {
                dropVal = 0f
            }

            var fxOffset = 0f
            var txOffset = 0f
            var initialXOffset = 0f
            var fyOffset = 0f
            var tyOffset = 0f
            var xTravelOffset = 0f
            var initialYOffset = 0f
            val wOff = skillWidthExpanded - skillWidth
            val hOff = (exSkill?.rect?.height() ?: 0f) - skillHeight
            exSkill.ifLet {
                if (it.gridX < fx) {
                    fxOffset = wOff
                }
                if (it.gridX < tx) {
                    txOffset = wOff
                }
                if (it.gridY < fy) {
                    fyOffset = hOff
                }
                if (it.gridY < ty) {
                    tyOffset = hOff
                }
                if (it.gridX == fx && it.gridY == fy) {
                    initialYOffset = hOff
                    initialXOffset = wOff
                }
                if (it.gridY == fy && connection.from.isLowered && fx != it.gridX) {
                    fyOffset += hOff
                }
                if (it.gridY == ty && connection.to.isLowered && tx != it.gridX) {
                    tyOffset += hOff
                }
                if (it.gridX == fx && it.gridY > fy) {
                    xTravelOffset = wOff
                }
            }

            // Account for xp row
            if (connection.fromCategoryId > Constants.SpecificSkillCategories.BEGINNER_SKILLS) {
                fxOffset += xpCostWidth.toInt()
                txOffset += xpCostWidth.toInt()
            }

            linePaint.color = connection.color
            // DownLine
            canvas.drawLine(x + fxOffset, y + initialYOffset + fyOffset, x + fxOffset, y + (lineHeight * lineMult) + dropVal + initialYOffset + fyOffset, linePaint)
            if (fx == tx) {
                // Across
                canvas.drawLine(x + fxOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, targetX + txOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, linePaint)
                // Down to skill
                canvas.drawLine(targetX + txOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, targetX + txOffset, targetY + tyOffset, linePaint)
            } else {
                val signX = (targetX - x).sign
                val xLoc = if (signX < 0) {
                    // Left
                    x - (skillWidth * mult) - (mult * spacingWidth)
                } else {
                    // Right
                    x + (skillWidth * (1f - mult)) + (mult * spacingWidth) + xTravelOffset
                }
                // Cross to space
                canvas.drawLine(x + fxOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, xLoc + fxOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, linePaint)
                if (connection.prereqs > 1) {
                    // Up or down to skill height
                    canvas.drawLine(xLoc + fxOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, xLoc + fxOffset, targetY - (spacingHeight * mult) + tyOffset, linePaint)
                    // Cross to skill
                    canvas.drawLine(xLoc + fxOffset, targetY - (spacingHeight * mult) + tyOffset, targetX + (-signX * (skillWidth / 4f)) + txOffset, targetY - (spacingHeight * mult) + tyOffset, linePaint)
                    canvas.drawLine(targetX + (-signX * (skillWidth / 4f)) + txOffset, targetY - (spacingHeight * mult) + tyOffset, targetX + txOffset, targetY - (spacingHeight / 2f) + tyOffset, linePaint)
                    canvas.drawLine(targetX + txOffset, targetY - (spacingHeight / 2f) + tyOffset, targetX + txOffset, targetY + tyOffset, linePaint)
                } else {
                    // Up or down to skill height
                    canvas.drawLine(xLoc + fxOffset, y + (lineHeight * lineMult) + dropVal + fyOffset + initialYOffset, xLoc + fxOffset, targetY - (spacingHeight / 2f) + tyOffset, linePaint)
                    // Cross to skill
                    canvas.drawLine(xLoc + fxOffset, targetY - (spacingHeight / 2f) + tyOffset, targetX + txOffset, targetY - (spacingHeight / 2f) + tyOffset, linePaint)
                    canvas.drawLine(targetX + txOffset, targetY - (spacingHeight / 2f) + tyOffset, targetX + txOffset, targetY + tyOffset, linePaint)
                }
                if (connection.prereqs > 1) {
                    circles[Shapes.Circle(targetX + txOffset, targetY - (spacingHeight / 2f) + tyOffset, numberCircleRadius)] = connection.prereqs
                }
            }
        }

        // Draw Intersection Circles
        circles.forEach { (it, num) ->
            circlePaint.shader = LinearGradient(it.x, it.y - (numberCircleRadius / 2f), it.x, it.y + (numberCircleRadius / 2f), lightGray, darkGray, Shader.TileMode.CLAMP)
            canvas.drawCircle(it.x, it.y, it.radius, circlePaint)
            val intersectionNum = StaticLayout.Builder.obtain(num.toString(), 0, num.toString().length, textPaint, (numberCircleRadius * 2).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .setIncludePad(false)
                .build()
            val intersectionNumOutline = StaticLayout.Builder.obtain(num.toString(), 0, num.toString().length, textPaintOutline, (numberCircleRadius * 2).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .setIncludePad(false)
                .build()
            val intersecHeight = intersectionNumOutline.height
            canvas.save()
            canvas.translate(it.x, it.y - (intersecHeight / 2))
            intersectionNumOutline.draw(canvas)
            intersectionNum.draw(canvas)
            canvas.restore()
        }

        // Draw Skill Requirements
        skillRequirements.forEach { skr ->
            val rect = skr.rect
            canvas.drawRect(rect, blackFill)
            canvas.drawRect(rect, outline)


            canvas.save()
            canvas.translate(rect.centerX(), rect.top)
            skr.layout.draw(canvas)
            canvas.restore()
        }
    }

    private fun orderCategories() {
        this.gridCategories.sortBy { it.skillCategoryId }
    }

    private fun calculateFullGrid(): MutableList<MutableList<OldFullSkillModel?>> {
        val grid: MutableList<MutableList<OldFullSkillModel?>> = mutableListOf()
        grid.add(mutableListOf())
        grid.add(mutableListOf())
        grid.add(mutableListOf())
        grid.add(mutableListOf())
        this.gridCategories.forEachIndexed { index, skillGridCategory ->
            skillGridCategory.branches.forEach { branch ->
                branch.grid.forEachIndexed { xpCost, row ->
                    row.forEach {
                        grid[xpCost].add(it)
                    }
                }
            }
        }
        return grid
    }

    private fun buildConnections(): List<GridConnection> {
        val gridConnections: MutableList<GridConnection> = mutableListOf()
        for(y in 0 until fullGrid.count()) {
            for(x in 0 until fullGrid[y].count()) {
                val skill = fullGrid[y][x]
                val postReqCount = skill?.postreqs?.count()?.toFloat() ?: 1f
                val increment = 1f / (postReqCount + 1f)
                var index = 0
                skill?.postreqs?.forEach { postreqId ->
                    getGridLocation(postreqId).ifLet {
                        gridConnections.add(GridConnection(GridLocation(x, y, skill.prereqs.firstOrNull { pre -> skill.xpCost.toInt() == pre.xpCost.toInt() } != null), it, increment, getSkillConnectionColor(skill.skillTypeId.toInt()), getSkill(postreqId).prereqs.count(), skill.skillCategoryId.toInt()))
                        index += 1
                    }
                }
            }
        }
        val cons = gridConnections.sortedWith(
            compareBy<GridConnection> { it.from.x}.thenBy { it.from.y }.thenBy { it.directionPriority() }.thenBy { it.distance() }
        ).toList()

        var prevX = 0
        var prevY = 0
        var count = 0
        cons.forEachIndexed { index, gc ->
            if (gc.from.x != prevX || gc.from.y != prevY) {
                prevX = gc.from.x
                prevY = gc.from.y
                count = 0
            }
            count += 1
            cons[index].mult *= count
        }

        return cons
    }

    private fun getSkill(skillId: Int): OldFullSkillModel {
        return skills.first { it.id == skillId }
    }

    private fun getGridLocation(skillId: Int): GridLocation? {
        for(y in 0 until fullGrid.count()) {
            for(x in 0 until fullGrid[y].count()) {
                val skill = fullGrid[y][x]
                if (skill?.id == skillId) {
                    return GridLocation(x, y, skill.prereqs.firstOrNull { pre -> skill.xpCost.toInt() == pre.xpCost.toInt() } != null)
                }
            }
        }
        return null
    }

    fun handleTap(x: Float, y: Float, context: Context, lifecycleScope: LifecycleCoroutineScope) {
        if (!makingPurchase) {
            val pb = purchaseButton?.copy()
            if (pb != null && pb.rect.contains(x, y) && couldPurchaseSkill(pb.skill.toFullSkillModel())) {
                purchaseSkill(pb, context, lifecycleScope)
            } else {
                val index = trueGrid.indexOfFirst { it.rect.contains(x, y) }
                if (index == -1) { return }
                val expanded = trueGrid[index].expanded
                for (i in 0 until trueGrid.count()) {
                    trueGrid[i].expanded = false
                }
                trueGrid[index].expanded = !expanded
                this.trueGrid = calculateTrueGrid()
                recalculateDottedLines()
            }
        }
    }

    private fun purchaseSkill(pb: TappablePurchaseButton, context: Context, lifecycleScope: LifecycleCoroutineScope) {
        makingPurchase = true
        dotHandler.post(dotRunnable)
        var msgStr = "It will cost you "
        val skl = pb.skill
        val player = OldDataManager.shared.selectedPlayer!!
        val char = OldDataManager.shared.charForSelectedPlayer!!
        var xpSpent = 0
        var fsSpent = 0
        var ppSpent = 0
        if (skl.canUseFreeSkill() && player.freeTier1Skills.toInt() > 0) {
            msgStr += "1 Free Tier-1 Skill point (you have ${player.freeTier1Skills} FT1S)"
            fsSpent = 1
        } else {
            msgStr += "${skl.modXpCost}xp (you have ${player.experience}xp)"
            xpSpent = skl.modXpCost.toInt()
        }

        if (skl.usesPrestige()) {
            msgStr += " and 1 Prestige point (you have ${player.prestigePoints}pp)"
            ppSpent = 1
        }
        AlertUtils.displayOkCancelMessage(context, "Are you sure you want to purchase ${skl.name}?", msgStr, onClickOk = { _, _ ->
            val charSkill = CharacterSkillCreateModel(
                characterId = char.id,
                skillId = skl.id,
                xpSpent = xpSpent,
                fsSpent = fsSpent,
                ppSpent = ppSpent
            )

            val charTakeSkillRequest = CharacterSkillService.TakeCharacterSkill()
            lifecycleScope.launch {
                charTakeSkillRequest.successfulResponse(CharacterSkillCreateSP(player.id, charSkill)).ifLet({
                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.CHARACTER, OldDataManagerType.PLAYER), true) {
                        AlertUtils.displayOkMessage(context, "${skl.name} Purchased!", "") { _, _ -> }

                        val xp = player.experience.toInt()
                        val fs = player.freeTier1Skills.toInt()
                        val pp = player.prestigePoints.toInt()

                        OldDataManager.shared.selectedPlayer = PlayerModel(
                            id = player.id,
                            username = player.username,
                            fullName = player.fullName,
                            startDate = player.startDate,
                            experience = "${xp - xpSpent}",
                            freeTier1Skills = "${fs - fsSpent}",
                            prestigePoints = "${pp - ppSpent}",
                            isCheckedIn = player.isCheckedIn,
                            isCheckedInAsNpc = player.isCheckedInAsNpc,
                            lastCheckIn = player.lastCheckIn,
                            numEventsAttended = player.numEventsAttended,
                            numNpcEventsAttended = player.numNpcEventsAttended,
                            isAdmin = player.isAdmin
                        )

                        OldDataManager.shared.charForSelectedPlayer!!.skills += arrayOf(skl.toFullSkillModel())
                        purchaseableSkills = getAvailableSkills(skills, OldDataManager.shared.selectedPlayer, OldDataManager.shared.charForSelectedPlayer, OldDataManager.shared.xpReductions)
                        for (i in 0 until trueGrid.count()) {
                            trueGrid[i].expanded = false
                        }
                        trueGrid = calculateTrueGrid()
                        recalculateDottedLines()
                        OldDataManager.shared.unrelaltedUpdateCallback()
                        makingPurchase = false
                        dotHandler.removeCallbacks(dotRunnable)
                        invalidate()
                    }
                }, {
                    AlertUtils.displaySomethingWentWrong(context)
                    makingPurchase = false
                    dotHandler.removeCallbacks(dotRunnable)
                    invalidate()
                })
            }
        }, onClickCancel = { _, _ ->
            makingPurchase = false
            dotHandler.removeCallbacks(dotRunnable)
            invalidate()
        })
    }

    fun getExapnded(): GridSkill? {
        val index = trueGrid.indexOfFirst { it.expanded }
        if (index == -1) { return null }
        val rect = trueGrid[index].rect
        trueGrid[index].rect = Shapes.rectf(rect.left, rect.top, skillWidthExpanded, calculateExpandedHeight(trueGrid[index]).toFloat())
        return trueGrid[index]
    }

    private fun calculateExpandedHeight(gs: GridSkill): Int {
        var totalHeight = 0
        val skill = gs.skill
        val sectionSpacing = textPadding * 4
        val lineHeight = 4

        // Title
        var text = skill.name
        var layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailTitlePaintOutline, (skillWidthExpanded.toInt() * 0.75).toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        val titleHeight = layout.height + textPadding + textPadding

        text = skill.getTypeText()
        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailSkillTypePaintOutline, (skillWidthExpanded.toInt() * 0.25).toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        val typeHeight = layout.height + textPadding + textPadding

        totalHeight += max(titleHeight, typeHeight) + sectionSpacing

        // Plus dividing line
        totalHeight += lineHeight + sectionSpacing

        text = skill.getFullCostText(purchaseableSkills)
        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailCostPaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        totalHeight += layout.height + textPadding + textPadding + sectionSpacing

        // Plus dividing line
        totalHeight += lineHeight + sectionSpacing

        if (skill.prereqs.isNotEmpty()) {
            text = "Prerequisites"
            layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqTitlePaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .setIncludePad(false)
                .build()
            totalHeight += layout.height + textPadding + textPadding

            skill.prereqs.forEach { prereq ->
                text = prereq.name
                layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqPaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                totalHeight += layout.height + textPadding + textPadding
            }
            totalHeight += sectionSpacing

            // Plus dividing line
            totalHeight += lineHeight + sectionSpacing
        }

        // Desc
        text = skill.description
        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailDescPaintOutline, skillWidthExpanded.toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        totalHeight += layout.height + textPadding + textPadding

        // Purchase skill button
        val pskill = purchaseableSkills.firstOrNull { skl -> skl.id == skill.id }
        if (personal && allowPurchase && pskill != null) {
            // Purchase Skill button
            totalHeight += 100 + (textPadding * 8) + (buttonOutlineHeight * 2).toInt()
        }
        return totalHeight
    }

    private fun calculateWidthAndHeightOfGridCategories() {
        // For Width
        // 1. Count the total number of skills per category
        // 2. Subtract the number of skills that are prerequisites for other skills in the same category. Only count each one once (i.e. use a Set)
        // 3. Find the highest number of skills for each XP rank
        // 4. Your width is the max between the answers from 2 and 3.

        // For Height (Determined globally) just assume 2 for now
        // If you never need to calculate it,
        // Count the maximum number of skills that are prerequisites for other skills, both of the same xp value (right now it's 2)

        val skillsCategorized: MutableMap<String, MutableList<OldFullSkillModel>> = mutableMapOf()
        for (skill in skills) {
            if (skillsCategorized[skill.skillCategoryId] == null) {
                val mutableList: MutableList<OldFullSkillModel> = mutableListOf(skill)
                skillsCategorized[skill.skillCategoryId] = mutableList
            } else {
                skillsCategorized[skill.skillCategoryId]?.add(skill)
            }
        }

        for ((category, skills) in skillsCategorized) {
            gridCategories.add(SkillGridCategory(skills, category.toInt(), skillCategories.first { it.id == category.toInt() }.name, this.skills))
        }
    }

    private fun calculateTrueGrid(): List<GridSkill> {

        var exExists = false
        var exTier = 0
        var exHeightDifference = 0f
        var exIsLower = false
        var exXLoc = 0
        var exSkillId = -1
        getExapnded().ifLet { exSkill ->
            exExists = true
            exTier = max(exSkill.skill.xpCost.toInt() - 1, 0)
            exHeightDifference = exSkill.rect.height() - skillHeight
            exIsLower = exSkill.skill.prereqs.firstOrNull { exSkill.skill.xpCost.toInt() == it.xpCost.toInt() && it.skillCategoryId == exSkill.skill.skillCategoryId } != null
            exXLoc = exSkill.gridX
            exSkillId = exSkill.skill.id
        }

        val gridSkills: MutableList<GridSkill> = mutableListOf()
        this.fullGrid.forEachIndexed { xpCost, row ->
            row.forEachIndexed { skillIndex, skl ->
                skl.ifLet { skill ->
                    var y = if (skill.prereqs.firstOrNull { skill.xpCost.toInt() == it.xpCost.toInt() && it.skillCategoryId == skill.skillCategoryId } == null) {
                        (xpCost * skillHeight * 2) + (xpCost * spacingHeight * 4) + spacingHeight + titleSize + titleSpacing
                    } else {
                        (xpCost * skillHeight * 2) + (xpCost * spacingHeight * 4) + skillHeight + spacingHeight + spacingHeight + titleSize + titleSpacing
                    }
                    var xoffset = 0f
                    if (skill.skillCategoryId.toInt() > Constants.SpecificSkillCategories.BEGINNER_SKILLS) {
                        xoffset = xpCostWidth
                    }
                    var x = spacingWidth + (skillIndex * skillWidth) + (skillIndex * spacingWidth * 2) + xoffset

                    if (exExists) {
                        if (skillIndex > exXLoc) {
                            x += (skillWidthExpanded - skillWidth)
                        }
                        if (xpCost > exTier) {
                            y += exHeightDifference
                        } else if (xpCost == exTier) {
                            if (!exIsLower && (skill.prereqs.firstOrNull { skill.xpCost.toInt() == it.xpCost.toInt() && it.skillCategoryId == skill.skillCategoryId } != null)) {
                                y += exHeightDifference
                            }
                        }
                    }
                    if (skill.id == exSkillId) {
                        gridSkills.add(GridSkill(Shapes.rectf(x, y, skillWidthExpanded, skillHeight + exHeightDifference), skill, skillIndex, xpCost, true))
                    } else {
                        gridSkills.add(GridSkill(Shapes.rectf(x, y, skillWidth, skillHeight), skill, skillIndex, xpCost))
                    }
                }
            }
        }
        return gridSkills.toList()
    }

    private fun recalculateDottedLines() {
        firstLineYOffset = 0f
        secondLineYOffset = 0f
        thirdLineYOffset = 0f
        lineStartXOffset = 0f
        lineEndXOffset = 0f
        getExapnded().ifLet {
            val offset = it.rect.height() - skillHeight
            lineEndXOffset = it.rect.width() - skillWidth
            when(it.gridY) {
                0 -> {
                    firstLineYOffset = offset
                    secondLineYOffset = offset
                    thirdLineYOffset = offset
                }
                1 -> {
                    secondLineYOffset = offset
                    thirdLineYOffset = offset
                }
                2 -> {
                    thirdLineYOffset = offset
                }
            }
            if (it.skill.skillCategoryId.toInt() == Constants.SpecificSkillCategories.BEGINNER_SKILLS) {
                lineStartXOffset = it.rect.width() - skillWidth
            }
        }
    }

}

data class GridSkill(var rect: RectF, val skill: OldFullSkillModel, val gridX: Int, val gridY: Int, var expanded: Boolean = false)

data class SkillRequirement(var rect: RectF, val layout: StaticLayout)

data class GridConnection(val from: GridLocation, val to: GridLocation, var mult: Float, var color: Int, var prereqs: Int, val fromCategoryId: Int) {
    fun distance(): Float {
        return ((to.x - from.x).toFloat().pow(2f) + (to.y - from.y).toFloat().pow(2f)).pow(0.5f)
    }

    fun directionPriority(): Int {
        return if (to.x < from.x) 0 else if (to.x == from.x) 1 else 2
    }
}
data class GridLocation(val x: Int, val y: Int, val isLowered: Boolean)

data class TappablePurchaseButton(val skill: CharacterModifiedSkillModel, val rect: RectF)