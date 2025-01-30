package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.Shapes
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.globalTestPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlin.math.max

class SkillGrid(skills: List<FullSkillModel>, skillCategories: List<SkillCategoryModel>) {
    private val skills: List<FullSkillModel>
    private val skillCategories: List<SkillCategoryModel>

    val gridCategories: MutableList<SkillGridCategory> = mutableListOf()

    private val skillWidth = 300f
    private val skillHeight = 300f
    private val spacingWidth = 75f
    private val spacingHeight = 75f
    private val textSize = 30f
    private val titleSize = 50f
    private val titleSpacing = 20f

    private val paint = Paint()
    private val blackPaint = Paint()
    private val outline = Paint()
    private val textPaint = TextPaint()
    private val titlePaint = TextPaint()
    private val dottedLine = Paint()

    // ARGB
    private val lightRed = Color.parseColor("#F7C9C6")
    private val darkRed = Color.parseColor("#EA6E69")

    private val lightBlue = Color.parseColor("#D8E7FB")
    private val darkBlue = Color.parseColor("#7FA7E0")

    private val lightGreen = Color.parseColor("#CAE1C5")
    private val darkGreen = Color.parseColor("#98D078")

    private val firstTierLine = Path().apply{
        moveTo(0f, (2f * skillHeight) + (4f * spacingHeight))
        lineTo(100000f, (2f * skillHeight) + (4f * spacingHeight)) // End POint
    }

    private val secondTierLine = Path().apply{
        moveTo(0f, (4f * skillHeight) + (8f * spacingHeight))
        lineTo(100000f, (4f * skillHeight) + (8f * spacingHeight)) // End POint
    }

    private val thirdTierLine = Path().apply{
        moveTo(0f, (6f * skillHeight) + (12f * spacingHeight))
        lineTo(100000f, (6f * skillHeight) + (12f * spacingHeight)) // End POint
    }

    init {
        this.skills = skills
        this.skillCategories = skillCategories

        blackPaint.color = Color.BLACK
        blackPaint.strokeWidth = 10F
        blackPaint.textSize = 50F

        outline.color = Color.WHITE
        outline.style = Paint.Style.STROKE
        outline.strokeWidth = 5F

        textPaint.color = Color.BLACK
        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 1F
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER

        titlePaint.color = Color.WHITE
        titlePaint.style = Paint.Style.FILL
        titlePaint.strokeWidth = 1F
        titlePaint.textSize = titleSize
        titlePaint.textAlign = Paint.Align.CENTER

        dottedLine.color = Color.WHITE
        dottedLine.style = Paint.Style.STROKE
        dottedLine.strokeWidth = 5F
        dottedLine.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)

        calculateWidthAndHeightOfGridCategories()
        orderCategories()
    }


    fun getSkillColor(x: Float, y: Float, typeId: Int): Paint {
        var gradient: LinearGradient? = null
        when (typeId) {
            Constants.SkillTypes.combat -> {
                gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightRed, darkRed, Shader.TileMode.CLAMP)
            }
            Constants.SkillTypes.profession -> {
                gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightGreen, darkGreen, Shader.TileMode.CLAMP)
            }
            Constants.SkillTypes.talent -> {
                gradient = LinearGradient(x+(skillWidth/2), y, x+(skillWidth/2), y+skillHeight, lightBlue, darkBlue, Shader.TileMode.CLAMP)
            }
        }
        paint.isDither = true
        paint.shader = gradient
        return paint
    }

    fun draw(canvas: Canvas, scaleFactor: Float) {
        canvas.save()
        dottedLine.pathEffect = DashPathEffect(floatArrayOf(30f, 30f), 0f)
        // Draw Horizontal Lines
        canvas.drawPath(firstTierLine, dottedLine)
        canvas.drawPath(secondTierLine, dottedLine)
        canvas.drawPath(thirdTierLine, dottedLine)
        canvas.restore()


        // Draw Skills
        var widthSoFar = 0f
        gridCategories.forEach { skillGridCategory ->
            // Outline Category and Name it
            canvas.drawRect(Shapes.rectf(widthSoFar, 0f, skillGridCategory.width * skillWidth + (skillGridCategory.width * 2 * spacingWidth), 8 * skillHeight + (spacingHeight * 16)), outline)
            val titleLayout = StaticLayout.Builder.obtain(skillGridCategory.skillCategoryName, 0, skillGridCategory.skillCategoryName.length, titlePaint, skillGridCategory.width * 2 * skillWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .setIncludePad(false)
                .build()
            canvas.save()
            canvas.translate(widthSoFar + ((skillGridCategory.width * skillWidth + (spacingWidth * skillGridCategory.width * 2)) / 2f), titleSize)
            titleLayout.draw(canvas)
            canvas.restore()

            skillGridCategory.branches.forEach { branch ->
                branch.grid.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { skillIndex, skl ->
                        skl.ifLet { skill ->
                            val cost = skill.xpCost.toInt()
                            val y = if (skill.prereqs.firstOrNull { cost == it.xpCost.toInt() } == null) {
                                (rowIndex * skillHeight * 2) + (rowIndex * spacingHeight * 4) + spacingHeight + titleSize + titleSpacing
                            } else {
                                (rowIndex * skillHeight * 2) + (rowIndex * spacingHeight * 4) + (skillHeight * 2) + spacingHeight + titleSize + titleSpacing
                            }
                            val x = widthSoFar + spacingWidth + (skillIndex * skillWidth) + (skillIndex * spacingWidth * 2)
                            canvas.drawRect(Shapes.rectf(x, y, skillWidth, skillHeight), getSkillColor(x, y, skill.skillTypeId.toInt()))
                            val titleLayout = StaticLayout.Builder.obtain(skill.name, 0, skill.name.length, textPaint, skillWidth.toInt())
                                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                                .setLineSpacing(1.5f, 1f)
                                .setIncludePad(false)
                                .build()
                            // Draw the text within the box (startX, startY is the top-left position)
                            canvas.save()
                            canvas.translate(x + (skillWidth / 2f), y)
                            titleLayout.draw(canvas)
                            canvas.restore()
                        }
                    }
                }
                widthSoFar += (branch.width * skillWidth) + (branch.width * spacingWidth * 2)
            }
        }
    }

    private fun orderCategories() {
        // TODO
        this.gridCategories.sortBy { it.skillCategoryId }
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

        val skillsCategorized: MutableMap<String, MutableList<FullSkillModel>> = mutableMapOf()
        for (skill in skills) {
            if (skillsCategorized[skill.skillCategoryId] == null) {
                val mutableList: MutableList<FullSkillModel> = mutableListOf(skill)
                skillsCategorized[skill.skillCategoryId] = mutableList
            } else {
                skillsCategorized[skill.skillCategoryId]?.add(skill)
            }
        }

        for ((category, skills) in skillsCategorized) {
            gridCategories.add(SkillGridCategory(skills, category.toInt(), skillCategories.first { it.id == category.toInt() }.name, this.skills))
        }
    }

}