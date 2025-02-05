package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.Shapes
import com.forkbombsquad.stillalivelarp.utils.globalTestPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sign

class SkillGrid(skills: List<FullSkillModel>, skillCategories: List<SkillCategoryModel>) {
    private val skills: List<FullSkillModel>
    private val skillCategories: List<SkillCategoryModel>
    private val gridCategories: MutableList<SkillGridCategory> = mutableListOf()
    private var trueGrid: List<GridSkill> = listOf()

    private val skillWidth = 300f
    private val skillWidthExpanded = 600f
    private val skillHeight = 300f
    private val spacingWidth = 75f
    private val spacingHeight = 150f
    private val lineHeight = spacingHeight / 2f
    private val textSize = 35f
    private val titleSize = 50f
    private val titleSpacing = 20f
    private val numberCircleRadius = 50f
    private val textPadding = 4

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
    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = this@SkillGrid.textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val skillDetailTitlePaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 40f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailSkillTypePaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 30f
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailCostPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailPrereqTitlePaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val skillDetailPrereqPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
        textSize = 35f
        textAlign = Paint.Align.CENTER
    }
    private val skillDetailDescPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1F
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

    // ARGB
    private val lightGray = Color.parseColor("#DDDDDD")
    private val darkGray = Color.parseColor("#999999")

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

    val fullGrid: MutableList<MutableList<FullSkillModel?>>
    val gridConnections: List<GridConnection>

    init {
        this.skills = skills
        this.skillCategories = skillCategories

        calculateWidthAndHeightOfGridCategories()
        orderCategories()
        fullGrid = calculateFullGrid()
        gridConnections = buildConnections()
        trueGrid = calculateTrueGrid()
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

    fun getSkillColor(typeId: Int): Int {
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

    fun draw(canvas: Canvas, scaleFactor: Float) {
        canvas.save()
        dottedLine.pathEffect = DashPathEffect(floatArrayOf(30f, 30f), 0f)
        // Draw Horizontal Lines
        canvas.drawPath(firstTierLine, dottedLine)
        canvas.drawPath(secondTierLine, dottedLine)
        canvas.drawPath(thirdTierLine, dottedLine)
        canvas.restore()

        // Draw Skills
        val exSkill = getExapnded()
        var widthSoFar = 0f
        gridCategories.forEachIndexed { index, skillGridCategory ->
            // Outline Category and Name it
            var extraXoffset = 0f
            var extraWidth = 0f
            exSkill.ifLet {
                if (it.skill.skillCategoryId.toInt() - 1 < index) {
                    extraXoffset = skillWidthExpanded - skillWidth
                }
                if (it.skill.skillCategoryId.toInt() - 1 == index) {
                    extraWidth = skillWidthExpanded - skillWidth
                }
            }
            canvas.drawRect(Shapes.rectf(widthSoFar + extraXoffset, 0f, skillGridCategory.width * skillWidth + extraWidth + (skillGridCategory.width * 2 * spacingWidth), 8 * skillHeight + (spacingHeight * 16)), outline)
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
        }
        var x = 0f
        var y = 0f
        this.trueGrid.forEach {
            x = it.rect.left
            y = it.rect.top
            val sectionSpacing = textPadding * 4
            val lineHeight = 4

            canvas.drawRect(it.rect, getSkillColor(x, y, it.skill.skillTypeId.toInt()))
            if (it.expanded) {
                var heightSoFar = 0f
                val skill = it.skill
                // Title
                var text = skill.name
                var layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailTitlePaint, (skillWidthExpanded.toInt() * 0.75).toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val titleHeight = layout.height + textPadding + textPadding
                canvas.save()
                canvas.translate(x + textPadding, y + textPadding)
                layout.draw(canvas)
                canvas.restore()

                text = skill.getTypeText()
                layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailSkillTypePaint, (skillWidthExpanded.toInt() * 0.25).toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val typeHeight = layout.height + textPadding + textPadding
                canvas.save()
                canvas.translate(x + skillWidthExpanded - textPadding - textPadding, y + textPadding)
                layout.draw(canvas)
                canvas.restore()

                heightSoFar += max(titleHeight, typeHeight) + sectionSpacing

                canvas.drawLine(x + sectionSpacing, y + heightSoFar, x + skillWidthExpanded - sectionSpacing, y + heightSoFar, dividingLinePaint)
                // Plus dividing line
                heightSoFar += lineHeight + sectionSpacing

                text = skill.getFullCostText()
                layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailCostPaint, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                layout.draw(canvas)
                canvas.restore()
                heightSoFar += layout.height + textPadding + textPadding + sectionSpacing

                canvas.drawLine(x + sectionSpacing, y + heightSoFar, x + skillWidthExpanded - sectionSpacing, y + heightSoFar, dividingLinePaint)
                // Plus dividing line
                heightSoFar += lineHeight + sectionSpacing

                if (skill.prereqs.isNotEmpty()) {
                    text = "Prerequisites"
                    layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqTitlePaint, skillWidthExpanded.toInt() - (textPadding * 2))
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(1.5f, 1f)
                        .setIncludePad(false)
                        .build()
                    canvas.save()
                    canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                    layout.draw(canvas)
                    canvas.restore()
                    heightSoFar += layout.height + textPadding + textPadding

                    skill.prereqs.forEach { prereq ->
                        text = prereq.name
                        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqPaint, skillWidthExpanded.toInt() - (textPadding * 2))
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setLineSpacing(1.5f, 1f)
                            .setIncludePad(false)
                            .build()
                        canvas.save()
                        canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                        layout.draw(canvas)
                        canvas.restore()
                        heightSoFar += layout.height + textPadding + textPadding
                    }
                    heightSoFar += sectionSpacing
                    canvas.drawLine(x + sectionSpacing, y + heightSoFar, x + skillWidthExpanded - sectionSpacing, y + heightSoFar, dividingLinePaint)
                    // Plus dividing line
                    heightSoFar += lineHeight + sectionSpacing
                }

                text = skill.description
                layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailDescPaint, skillWidthExpanded.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(x + textPadding + (skillWidthExpanded / 2), y + textPadding + heightSoFar)
                layout.draw(canvas)
                canvas.restore()
            } else {
                // Draw the text centered within the box (startX, startY is the top-left position)
                val titleLayout = StaticLayout.Builder.obtain(it.skill.name, 0, it.skill.name.length, textPaint, skillWidth.toInt() - (textPadding * 2))
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.5f, 1f)
                    .setIncludePad(false)
                    .build()
                val textHeight = titleLayout.height
                canvas.save()
                canvas.translate(x + (skillWidth / 2f), y + (skillHeight / 2f) - (textHeight / 2f))
                titleLayout.draw(canvas)
                canvas.restore()
            }
        }

        val circles: MutableList<Shapes.Circle> = mutableListOf()

        gridConnections.forEach { connection ->
            val fx = connection.from.x
            val fy = connection.from.y
            val tx = connection.to.x
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

            var extraXoffset = 0f
            var extraYOffset = 0f
            var initialYOffest = 0f
            exSkill.ifLet {
                if (it.gridX < fx) {
                    extraXoffset = skillWidthExpanded - skillWidth
                }
                if (it.gridY < fy) {
                    extraYOffset = it.rect.height() - skillHeight
                }
                if (it.gridX == fx && it.gridY == fy) {
                    initialYOffest = it.rect.height() - skillHeight
                }
            }

            linePaint.color = connection.color
            // DownLine
            canvas.drawLine(x+ extraXoffset, y + extraYOffset + initialYOffest, x + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, linePaint)
            if (fx == tx) {
                // Across
                canvas.drawLine(x + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, targetX + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, linePaint)
                // Down to skill
                canvas.drawLine(targetX + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, targetX + extraXoffset, targetY + extraYOffset, linePaint)
            } else {
                val signX = (targetX - x).sign
                val xLoc = if (signX < 0) {
                    // Left
                    x - (skillWidth * mult) - (mult * spacingWidth)
                } else {
                    // Right
                    x + (skillWidth * (1f - mult)) + (mult * spacingWidth)
                }
                // Cross to space
                canvas.drawLine(x + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, xLoc + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, linePaint)
                if (connection.prereqs > 1) {
                    // Up or down to skill height
                    canvas.drawLine(xLoc + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, xLoc + extraXoffset, targetY - (spacingHeight * mult) + extraYOffset, linePaint)
                    // Cross to skill
                    canvas.drawLine(xLoc + extraXoffset, targetY - (spacingHeight * mult) + extraYOffset, targetX + extraXoffset + (-signX * (skillWidth / 4f)), targetY - (spacingHeight * mult) + extraYOffset, linePaint)
                    canvas.drawLine(targetX + (-signX * (skillWidth / 4f)) + extraXoffset, targetY - (spacingHeight * mult) + extraYOffset, targetX + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, linePaint)
                    canvas.drawLine(targetX + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, targetX + extraXoffset, targetY + extraYOffset, linePaint)
                } else {
                    // Up or down to skill height
                    canvas.drawLine(xLoc + extraXoffset, y + (lineHeight * lineMult) + dropVal + extraYOffset, xLoc + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, linePaint)
                    // Cross to skill
                    canvas.drawLine(xLoc + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, targetX + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, linePaint)
                    canvas.drawLine(targetX + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, targetX + extraXoffset, targetY + extraYOffset, linePaint)
                }
                if (connection.prereqs > 1) {
                    circles.add(Shapes.Circle(targetX + extraXoffset, targetY - (spacingHeight / 2f) + extraYOffset, numberCircleRadius))
                }
            }
        }
        circles.forEach {
            circlePaint.shader = LinearGradient(it.x, it.y - (numberCircleRadius / 2f), it.x, it.y + (numberCircleRadius / 2f), lightGray, darkGray, Shader.TileMode.CLAMP)
            canvas.drawCircle(it.x, it.y, it.radius, circlePaint)
        }
    }

    private fun orderCategories() {
        this.gridCategories.sortBy { it.skillCategoryId }
    }

    private fun calculateFullGrid(): MutableList<MutableList<FullSkillModel?>> {
        val grid: MutableList<MutableList<FullSkillModel?>> = mutableListOf()
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
                        gridConnections.add(GridConnection(GridLocation(x, y, skill.prereqs.firstOrNull { pre -> skill.xpCost.toInt() == pre.xpCost.toInt() } != null), it, increment, getSkillColor(skill.skillTypeId.toInt()), getSkill(postreqId).prereqs.count()))
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

    private fun getSkill(skillId: Int): FullSkillModel {
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

    fun handleTap(x: Float, y: Float) {
        val index = trueGrid.indexOfFirst { it.rect.contains(x, y) }
        if (index == -1) { return }
        val expanded = trueGrid[index].expanded
        for (i in 0 until trueGrid.count()) {
            trueGrid[i].expanded = false
        }
        trueGrid[index].expanded = !expanded
        this.trueGrid = calculateTrueGrid()
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
        var layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailTitlePaint, (skillWidthExpanded.toInt() * 0.75).toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        val titleHeight = layout.height + textPadding + textPadding

        text = skill.getTypeText()
        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailSkillTypePaint, (skillWidthExpanded.toInt() * 0.25).toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        val typeHeight = layout.height + textPadding + textPadding

        totalHeight += max(titleHeight, typeHeight) + sectionSpacing

        // Plus dividing line
        totalHeight += lineHeight + sectionSpacing

        text = skill.getFullCostText()
        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailCostPaint, skillWidthExpanded.toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        totalHeight += layout.height + textPadding + textPadding + sectionSpacing

        // Plus dividing line
        totalHeight += lineHeight + sectionSpacing

        if (skill.prereqs.isNotEmpty()) {
            text = "Prerequisites"
            layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqTitlePaint, skillWidthExpanded.toInt() - (textPadding * 2))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .setIncludePad(false)
                .build()
            totalHeight += layout.height + textPadding + textPadding

            skill.prereqs.forEach { prereq ->
                text = prereq.name
                layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailPrereqPaint, skillWidthExpanded.toInt() - (textPadding * 2))
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

        text = skill.description
        layout = StaticLayout.Builder.obtain(text, 0, text.length, skillDetailDescPaint, skillWidthExpanded.toInt() - (textPadding * 2))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .setIncludePad(false)
            .build()
        totalHeight += layout.height + textPadding + textPadding
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
                    var x = spacingWidth + (skillIndex * skillWidth) + (skillIndex * spacingWidth * 2)

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

}

data class GridSkill(var rect: RectF, val skill: FullSkillModel, val gridX: Int, val gridY: Int, var expanded: Boolean = false)

data class GridConnection(val from: GridLocation, val to: GridLocation, var mult: Float, var color: Int, var prereqs: Int) {
    fun distance(): Float {
        return ((to.x - from.x).toFloat().pow(2f) + (to.y - from.y).toFloat().pow(2f)).pow(0.5f)
    }

    fun directionPriority(): Int {
        return if (to.x < from.x) 0 else if (to.x == from.x) 1 else 2
    }
}
data class GridLocation(val x: Int, val y: Int, val isLowered: Boolean)