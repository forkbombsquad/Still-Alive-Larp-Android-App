package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.globalTestPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlin.math.max

class SkillGridCategory(skills: List<FullSkillModel>, skillCategoryId: Int, skillCategoryName: String, allSkills: List<FullSkillModel>) {
    val allSkills: List<FullSkillModel>
    var skills: List<FullSkillModel>
    val skillCategoryId: Int
    val skillCategoryName: String

    val zeroCost: MutableList<FullSkillModel> = mutableListOf()
    val oneCost: MutableList<FullSkillModel> = mutableListOf()
    val twoCost: MutableList<FullSkillModel> = mutableListOf()
    val threeCost: MutableList<FullSkillModel> = mutableListOf()
    val fourCost: MutableList<FullSkillModel> = mutableListOf()

    val branches: MutableList<SkillBranch> = mutableListOf()
    private var isEdgeCaseLeft = false
    private var isEdgeCaseRight = false
    private var edgeCaseLeft: SkillBranch? = null
    private var edgeCaseRight: SkillBranch? = null

    val width: Int
    init {
        this.allSkills = allSkills
        this.skills = skills
        this.skillCategoryId = skillCategoryId
        this.skillCategoryName = skillCategoryName
        sortSkills()
        buildBranches()
        this.width = calculateWidth()
    }

    private fun sortSkills() {
        for (skill in skills) {
            if (skill.xpCost.toInt() == 1) {
                oneCost.add(skill)
            } else if (skill.xpCost.toInt() == 2) {
                twoCost.add(skill)
            } else if (skill.xpCost.toInt() == 3) {
                threeCost.add(skill)
            } else if (skill.xpCost.toInt() == 4) {
                fourCost.add(skill)
            } else {
                zeroCost.add(skill)
            }
        }
        skills = skills.sortedBy { it.xpCost.toInt() }
    }

    private fun buildBranches() {
        skills.forEach {
            isEdgeCaseLeft = false
            isEdgeCaseRight = false
            val skillList: MutableList<FullSkillModel> = mutableListOf()
            buildBranchRec(it, skillList)
            if (skillList.isNotEmpty()) {
                if (isEdgeCaseLeft) {
                    edgeCaseLeft = SkillBranch(skillList, allSkills, skillCategoryId)
                } else if (isEdgeCaseRight) {
                    edgeCaseRight = SkillBranch(skillList, allSkills, skillCategoryId)
                } else {
                    branches.add(SkillBranch(skillList, allSkills, skillCategoryId))
                }
            }
        }
        edgeCaseLeft.ifLet {
            branches.add(0, it)
        }
        edgeCaseRight.ifLet {
            branches.add(it)
        }
    }

    private fun buildBranchRec(skill: FullSkillModel?, list: MutableList<FullSkillModel>, isPrereq: Boolean = false) {
        if (skill != null) {
            if (branchesAlreadyContain(skill.id) || list.firstOrNull { it.id == skill.id } != null || edgeCaseLeft?.skills?.firstOrNull { it.id == skill.id } != null || edgeCaseRight?.skills?.firstOrNull { it.id == skill.id } != null) { return }
            if (skillCategoryId > skill.skillCategoryId.toInt()) {
                isEdgeCaseLeft = true
                return
            }
            if (skillCategoryId < skill.skillCategoryId.toInt()) {
                isEdgeCaseRight = true
                return
            }
            list.add(skill)
            if (!isPrereq) {
                skill.postreqs.forEach {
                    buildBranchRec(getSkill(it), list)
                }
            }
            skill.prereqs.forEach {
                buildBranchRec(it, list, true)
            }
        }
    }

    private fun getSkill(skillId: Int): FullSkillModel? {
        return allSkills.firstOrNull { it.id == skillId }
    }

    private fun branchesAlreadyContain(skillId: Int): Boolean {
        branches.forEach { skillBranch ->
            if (skillBranch.skills.firstOrNull { it.id == skillId } != null) {
                return true
            }
        }
        return false
    }

    private fun calculateWidth(): Int {
        // OLD WAY
//        // For Width
//        // 1. Count the total number of skills per category
//        var totalSkillsInCat = skills.count()
//        // 2. Subtract the number of skills that are prerequisites for other skills in the same category. Only count each one once (i.e. use a Set)
//        var setOfPrereqs: MutableSet<Int> = mutableSetOf()
//        for (skill in skills) {
//            for (prereq in skill.prereqs) {
//                if (prereq.skillCategoryId == skill.skillCategoryId) {
//                    setOfPrereqs.add(prereq.id)
//                }
//            }
//        }
//        // 3. Find the highest number of skills for each XP rank
//        var highestNumAmongSkills = max(
//            zeroCost.count(),
//            max(
//                oneCost.count(),
//                max(twoCost.count(), max(threeCost.count(), fourCost.count()))
//            )
//        )
//        // 4. Your width is the max between the answers from 2 and 3.
//        return max(highestNumAmongSkills, totalSkillsInCat - setOfPrereqs.count())

        return branches.sumOf { it.width }
    }

}