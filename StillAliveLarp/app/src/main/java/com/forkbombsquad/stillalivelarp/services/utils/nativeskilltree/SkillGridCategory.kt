package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import com.forkbombsquad.stillalivelarp.services.models.OldFullSkillModel
import com.forkbombsquad.stillalivelarp.utils.ifLet

class SkillGridCategory(skills: List<OldFullSkillModel>, skillCategoryId: Int, skillCategoryName: String, allSkills: List<OldFullSkillModel>) {
    val allSkills: List<OldFullSkillModel>
    var skills: List<OldFullSkillModel>
    val skillCategoryId: Int
    val skillCategoryName: String

    val zeroCost: MutableList<OldFullSkillModel> = mutableListOf()
    val oneCost: MutableList<OldFullSkillModel> = mutableListOf()
    val twoCost: MutableList<OldFullSkillModel> = mutableListOf()
    val threeCost: MutableList<OldFullSkillModel> = mutableListOf()
    val fourCost: MutableList<OldFullSkillModel> = mutableListOf()

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
            val skillList: MutableList<OldFullSkillModel> = mutableListOf()
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

    private fun buildBranchRec(skill: OldFullSkillModel?, list: MutableList<OldFullSkillModel>, isPrereq: Boolean = false) {
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

    private fun getSkill(skillId: Int): OldFullSkillModel? {
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
        return branches.sumOf { it.width }
    }

}