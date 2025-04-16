package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.ifLet

class SkillBranch(skills: List<FullSkillModel>, allSkills: List<FullSkillModel>, categoryId: Int) {

    val categoryId: Int
    val allSkills: List<FullSkillModel>
    val skills: List<FullSkillModel>
    var width: Int
    val grid: MutableList<MutableList<FullSkillModel?>>

    init {
        this.categoryId = categoryId
        this.allSkills = allSkills
        this.skills = skills.sortedBy { it.xpCost.toInt() }
        val counts = arrayOf(0, 0, 0, 0, 0)
        skills.forEach {
            counts[it.xpCost.toInt()] += 1
        }
        width = counts.max()
        grid = mutableListOf()
        organziePlacementGrid()
    }

    private fun organziePlacementGrid() {
        if (skills.firstOrNull { it.xpCost.toInt() == 0 } != null) {
            // Free Skills
            grid.add(mutableListOf())
            grid.add(mutableListOf())
            grid.add(mutableListOf())
            grid.add(mutableListOf())
            skills.forEach {
                grid[0].add(it)
                grid[1].add(null)
                grid[2].add(null)
                grid[3].add(null)
            }

        } else {
            grid.addAll(listOf(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()))
            // Non Free Skills
            // Start from the top left corner
            skills.forEach { skill ->
                addSkillRec(skill, previousCost = -1)
            }

        }
        var index = 0
        while (index < grid.size) {
            while(grid[index].size < width) {
                grid[index].add(null)
            }
            index++
        }
    }

    private fun addSkillRec(skill: FullSkillModel?, previousCost: Int) {
        if (skill != null) {
            if (skillInGrid(skill)) { return }
            if (skill.skillCategoryId.toInt() != categoryId) { return }
            val cost = skill.xpCost.toInt()
            grid[cost - 1].add(skill)

            // Add null spaces if there's a jump in the grid (i.e. a skill leads to another skill that skips a tier)
            if (previousCost != -1 && previousCost + 1 < cost) {
                var untilCost = previousCost + 1
                while (untilCost < cost) {
                    grid[untilCost - 1].add(null)
                    untilCost += 1
                }
            }

            skill.postreqs.forEach { postreqId ->
                val postreq = getSkill(postreqId)
                addSkillRec(postreq, cost)
            }
        }
    }

    fun skillInGrid(skill: FullSkillModel): Boolean {
        grid.forEach {
            if (it.firstOrNull { sk -> sk?.id == skill.id } != null) {
                return true
            }
        }
        return false
    }

    fun getSkill(skillId: Int): FullSkillModel? {
        return allSkills.firstOrNull() { it.id == skillId }
    }

    fun prettyPrintGrid(): String {
        var print = ""
        print += "["
        grid.forEach { list ->
            print += "\n  ["
            list.forEach { skill ->
                if (skill == null) {
                    print += "null, "
                } else {
                    print += "${skill.name} (${skill.id}), "
                }
            }
            print += "],"
        }
        print += "\n]"
        return print
    }

}

