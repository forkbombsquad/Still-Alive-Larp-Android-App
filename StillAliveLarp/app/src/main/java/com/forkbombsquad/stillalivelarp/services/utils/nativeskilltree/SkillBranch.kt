package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel

class SkillBranch(skills: List<FullCharacterModifiedSkillModel>, allSkills: List<FullCharacterModifiedSkillModel>, categoryId: Int) {

    val categoryId: Int
    val allSkills: List<FullCharacterModifiedSkillModel>
    val skills: List<FullCharacterModifiedSkillModel>
    var width: Int
    val grid: MutableList<MutableList<FullCharacterModifiedSkillModel?>>

    init {
        this.categoryId = categoryId
        this.allSkills = allSkills
        this.skills = skills.sortedBy { it.baseXpCost() }
        val counts = arrayOf(0, 0, 0, 0, 0)
        skills.forEach {
            counts[it.baseXpCost()] += 1
        }
        width = counts.max()
        grid = mutableListOf()
        organziePlacementGrid()
    }

    private fun organziePlacementGrid() {
        if (skills.firstOrNull { it.baseXpCost() == 0 } != null) {
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

    private fun addSkillRec(skill: FullCharacterModifiedSkillModel?, previousCost: Int) {
        if (skill != null) {
            if (skillInGrid(skill)) { return }
            if (skill.category.id != categoryId) { return }
            val cost = skill.baseXpCost()
            grid[cost - 1].add(skill)

            // Add null spaces if there's a jump in the grid (i.e. a skill leads to another skill that skips a tier)
            if (previousCost != -1 && previousCost + 1 < cost) {
                var untilCost = previousCost + 1
                while (untilCost < cost) {
                    grid[untilCost - 1].add(null)
                    untilCost += 1
                }
            }

            skill.postreqs().forEach { postreq ->
                val pr = getSkill(postreq.id)
                addSkillRec(pr, cost)
            }
        }
    }

    fun skillInGrid(skill: FullCharacterModifiedSkillModel): Boolean {
        grid.forEach {
            if (it.firstOrNull { sk -> sk?.id == skill.id } != null) {
                return true
            }
        }
        return false
    }

    fun getSkill(skillId: Int): FullCharacterModifiedSkillModel? {
        return allSkills.firstOrNull { it.id == skillId }
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

