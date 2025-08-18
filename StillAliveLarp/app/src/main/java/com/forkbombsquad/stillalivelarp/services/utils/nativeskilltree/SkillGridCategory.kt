package com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree

import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.utils.ifLet

class SkillGridCategory(skills: List<FullCharacterModifiedSkillModel>, skillCategoryId: Int, skillCategoryName: String, allSkills: List<FullCharacterModifiedSkillModel>) {
    val allSkills: List<FullCharacterModifiedSkillModel>
    var skills: List<FullCharacterModifiedSkillModel>
    val skillCategoryId: Int
    val skillCategoryName: String

    val zeroCost: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()
    val oneCost: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()
    val twoCost: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()
    val threeCost: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()
    val fourCost: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()

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
            if (skill.baseXpCost() == 1) {
                oneCost.add(skill)
            } else if (skill.baseXpCost() == 2) {
                twoCost.add(skill)
            } else if (skill.baseXpCost() == 3) {
                threeCost.add(skill)
            } else if (skill.baseXpCost() == 4) {
                fourCost.add(skill)
            } else {
                zeroCost.add(skill)
            }
        }
        skills = skills.sortedBy { it.baseXpCost() }
    }

    private fun buildBranches() {
        skills.forEach {
            isEdgeCaseLeft = false
            isEdgeCaseRight = false
            val skillList: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()
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

    private fun buildBranchRec(skill: FullCharacterModifiedSkillModel?, list: MutableList<FullCharacterModifiedSkillModel>, isPrereq: Boolean = false) {
        if (skill != null) {
            if (branchesAlreadyContain(skill.id) || list.firstOrNull { it.id == skill.id } != null || edgeCaseLeft?.skills?.firstOrNull { it.id == skill.id } != null || edgeCaseRight?.skills?.firstOrNull { it.id == skill.id } != null) { return }
            if (skillCategoryId > skill.category.id) {
                isEdgeCaseLeft = true
                return
            }
            if (skillCategoryId < skill.category.id) {
                isEdgeCaseRight = true
                return
            }
            list.add(skill)
            if (!isPrereq) {
                skill.postreqs().forEach {
                    buildBranchRec(getSkill(it.id), list)
                }
            }
            skill.prereqs().forEach {
                buildBranchRec(getSkill(it.id), list, true)
            }
        }
    }

    private fun getSkill(skillId: Int): FullCharacterModifiedSkillModel? {
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