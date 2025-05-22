package com.forkbombsquad.stillalivelarp.services.managers

import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.SkillPrereqService
import com.forkbombsquad.stillalivelarp.services.SkillService
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class SkillManager private constructor() {

    private var skills: MutableList<FullSkillModel>? = null
    private var fetching = false
    private var completionBlocks: MutableList<(skills: List<FullSkillModel>?) -> Unit> = mutableListOf()

    fun getSkills(lifecycleScope: LifecycleCoroutineScope, overrideLocal: Boolean, callback: (skills: List<FullSkillModel>?) -> Unit) {
        if (!overrideLocal && skills != null) {
            callback(skills)
        } else {
            completionBlocks.add(callback)
            if (!fetching) {
                fetching = true
                val skillRequest = SkillService.GetAllSkills()
                lifecycleScope.launch {
                    skillRequest.successfulResponse().ifLet({ skillList ->
                        skills = mutableListOf()
                        skillList.skills.forEach { skill ->
                            skills?.add(FullSkillModel(skill))
                        }
                        val prereqRequest = SkillPrereqService.GetAllSkillPrereqs()
                        lifecycleScope.launch {
                            prereqRequest.successfulResponse().ifLet({ skillPrereqList ->
                                skills?.forEachIndexed { index, fullSkillModel ->
                                    // Prereqs
                                    skillPrereqList.skillPrereqs.filter { spr -> spr.baseSkillId == fullSkillModel.id }.forEach { prereq ->
                                        skills?.firstOrNull { sk -> sk.id == prereq.prereqSkillId }.ifLet { prereqSkill ->
                                            fullSkillModel.prereqs += arrayOf(prereqSkill)
                                            skills?.set(index, fullSkillModel)
                                        }
                                    }
                                }
                                skills?.forEachIndexed { index, fullSkillModel ->
                                    // Postreqs
                                    skillPrereqList.skillPrereqs.filter { spr -> spr.prereqSkillId == fullSkillModel.id }.forEach { prereq ->
                                        skills?.firstOrNull { sk -> sk.id == prereq.baseSkillId }.ifLet { prereqSkill ->
                                            fullSkillModel.postreqs += arrayOf(prereqSkill.id)
                                            skills?.set(index, fullSkillModel)
                                        }
                                    }
                                }
                                OldSharedPrefsManager.shared.storeSkills(skills ?: listOf())
                                fetching = false
                                completionBlocks.forEach { cb ->
                                    cb(skills)
                                }
                                completionBlocks = mutableListOf()
                            }, {
                                fetching = false
                                for (completionBlock in completionBlocks) {
                                    completionBlock(skills)
                                }
                                completionBlocks = mutableListOf()
                            })
                        }
                    }, {
                        fetching = false
                        for (completionBlock in completionBlocks) {
                            completionBlock(skills)
                        }
                        completionBlocks = mutableListOf()
                    })
                }
            }
        }
    }

    fun getSkillsOffline(): List<FullSkillModel> {
        return OldSharedPrefsManager.shared.getSkills()
    }

    companion object {
        val shared = SkillManager()
    }

}