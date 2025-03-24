package com.forkbombsquad.stillalivelarp.services.managers

import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class CharacterManager private constructor() {

    private var character: FullCharacterModel? = null
    private var fetching = false
    private var completionBlocks: MutableList<(character: FullCharacterModel?) -> Unit> = mutableListOf()

    fun forceReset() {
        character = null
    }

    fun fetchFullCharacter(lifecycleScope: LifecycleCoroutineScope, characterId: Int, callback: (character: FullCharacterModel?) -> Unit) {
        val request = CharacterService.GetCharacter()
        lifecycleScope.launch {
            request.successfulResponse(IdSP(characterId)).ifLet({
                val fulCharModel = FullCharacterModel(it)
                SkillManager.shared.getSkills(lifecycleScope, overrideLocal = false) { skills ->
                    skills.ifLet({ nonnullSkills ->
                        val cskillRequest = CharacterSkillService.GetAllCharacterSkillsForCharacter()
                        lifecycleScope.launch {
                            cskillRequest.successfulResponse(IdSP(characterId)).ifLet({ charSkills ->
                                charSkills.charSkills.forEach { charSkill ->
                                    nonnullSkills.firstOrNull { itSkill -> itSkill.id == charSkill.skillId }.ifLet { fsm ->
                                        fulCharModel.skills = fulCharModel.skills + arrayOf(fsm)
                                    }
                                }
                                callback(fulCharModel)
                            }, {
                                callback(fulCharModel)
                            })
                        }
                    }, {
                        callback(fulCharModel)
                    })
                }
            }, {
                callback(null)
            })
        }
    }

    fun getActiveCharacterForOtherPlayer(lifecycleScope: LifecycleCoroutineScope, playerId: Int, callback: (character: FullCharacterModel?) -> Unit) {
        val request = CharacterService.GetAllPlayerCharacters()
        lifecycleScope.launch {
            request.successfulResponse(IdSP(playerId)).ifLet({
                it.characters.firstOrNull { char -> char.isAlive.toBoolean() && char.characterTypeId == Constants.CharacterTypeId.standard }.ifLet({
                    val fullCharRequest = CharacterService.GetCharacter()
                    lifecycleScope.launch {
                        fullCharRequest.successfulResponse(IdSP(it.id)).ifLet({ charModel ->
                            val fulCharModel = FullCharacterModel(charModel)
                            SkillManager.shared.getSkills(lifecycleScope, overrideLocal = false) { skills ->
                                skills.ifLet({ nonnullSkills ->
                                    val cskillRequest = CharacterSkillService.GetAllCharacterSkillsForCharacter()
                                    lifecycleScope.launch {
                                        cskillRequest.successfulResponse(IdSP(charModel.id)).ifLet({ charSkills ->
                                            charSkills.charSkills.forEach { charSkill ->
                                                nonnullSkills.firstOrNull { itSkill -> itSkill.id == charSkill.skillId }.ifLet { fsm ->
                                                    fulCharModel.skills = fulCharModel.skills + arrayOf(fsm)
                                                }
                                            }
                                            callback(fulCharModel)
                                        }, {
                                            callback(fulCharModel)
                                        })
                                    }
                                }, {
                                    callback(fulCharModel)
                                })
                            }
                        }, {
                            callback(null)
                        })
                    }
                }, {
                    callback(null)
                })
            }, {
                callback(null)
            })
        }
    }

    fun fetchActiveCharacter(lifecycleScope: LifecycleCoroutineScope, overrideLocal: Boolean = false, callback: (character: FullCharacterModel?) -> Unit) {
        if (!overrideLocal && character != null) {
            callback(character)
        } else {
            PlayerManager.shared.getPlayer().ifLet({ playerModel ->
                completionBlocks.add(callback)
                if (!fetching) {
                    fetching = true
                    val allCharRequest = CharacterService.GetAllPlayerCharacters()
                    lifecycleScope.launch {
                        allCharRequest.successfulResponse(IdSP(playerModel.id)).ifLet({ characterListModel ->
                            characterListModel.characters.firstOrNull { it.isAlive.toBoolean() && it.characterTypeId == Constants.CharacterTypes.standard }.ifLet({ characterSubModel ->
                                val charRequest = CharacterService.GetCharacter()
                                lifecycleScope.launch {
                                    charRequest.successfulResponse(IdSP(characterSubModel.id)).ifLet({ characterModel ->
                                        val fullCharModel = FullCharacterModel(characterModel)
                                        SkillManager.shared.getSkills(lifecycleScope, overrideLocal = false) { skills ->
                                            skills.ifLet({
                                                val charSkillRequest = CharacterSkillService.GetAllCharacterSkillsForCharacter()
                                                lifecycleScope.launch {
                                                    charSkillRequest.successfulResponse(IdSP(fullCharModel.id)).ifLet({ characterSkillListModel ->
                                                        characterSkillListModel.charSkills.forEach { charSkill ->
                                                            skills?.firstOrNull { skill -> skill.id == charSkill.skillId }.ifLet { fsm ->
                                                                fullCharModel.skills = fullCharModel.skills + arrayOf(fsm)
                                                            }
                                                        }
                                                        this@CharacterManager.character = fullCharModel
                                                        SharedPrefsManager.shared.storeCharacter(fullCharModel)
                                                        fetching = false
                                                        completionBlocks.forEach { cb ->
                                                            cb(fullCharModel)
                                                        }
                                                        completionBlocks = mutableListOf()
                                                    }, {
                                                        fetching = false
                                                        this@CharacterManager.character = fullCharModel
                                                        completionBlocks.forEach { cb ->
                                                            cb(fullCharModel)
                                                        }
                                                        completionBlocks = mutableListOf()
                                                    })
                                                }
                                            }, {
                                                fetching = false
                                                this@CharacterManager.character = fullCharModel
                                                completionBlocks.forEach { cb ->
                                                    cb(fullCharModel)
                                                }
                                                completionBlocks = mutableListOf()
                                            })
                                        }
                                    }, {
                                        fetching = false
                                        this@CharacterManager.character = null
                                        completionBlocks.forEach { cb ->
                                            cb(null)
                                        }
                                        completionBlocks = mutableListOf()
                                    })
                                }
                            }, {
                                fetching = false
                                this@CharacterManager.character = null
                                completionBlocks.forEach { cb ->
                                    cb(null)
                                }
                                completionBlocks = mutableListOf()
                            })
                        }, {
                            fetching = false
                            this@CharacterManager.character = null
                            completionBlocks.forEach { cb ->
                                cb(null)
                            }
                            completionBlocks = mutableListOf()
                        })
                    }
                }
            }, {
                callback(null)
            })
        }
    }

    fun newCharacterCreated(lifecycleScope: LifecycleCoroutineScope, characterModel: CharacterModel) {
        val fullCharModel = FullCharacterModel(characterModel)
        fetching = true
        SkillManager.shared.getSkills(lifecycleScope, overrideLocal = false) { skills ->
            skills.ifLet({
                val charSkillRequest = CharacterSkillService.GetAllCharacterSkillsForCharacter()
                lifecycleScope.launch {
                    charSkillRequest.successfulResponse(IdSP(fullCharModel.id)).ifLet({ characterSkillListModel ->
                        characterSkillListModel.charSkills.forEach { charSkill ->
                            skills?.firstOrNull { skill -> skill.id == charSkill.skillId }.ifLet { fsm ->
                                fullCharModel.skills = fullCharModel.skills + arrayOf(fsm)
                            }
                        }
                        this@CharacterManager.character = fullCharModel
                        SharedPrefsManager.shared.storeCharacter(fullCharModel)
                        fetching = false
                        completionBlocks.forEach { cb ->
                            cb(fullCharModel)
                        }
                        completionBlocks = mutableListOf()
                    }, {
                        this@CharacterManager.character = fullCharModel
                        completionBlocks.forEach { cb ->
                            cb(fullCharModel)
                        }
                        completionBlocks = mutableListOf()
                    })
                }
            }, {
                this@CharacterManager.character = fullCharModel
                completionBlocks.forEach { cb ->
                    cb(fullCharModel)
                }
                completionBlocks = mutableListOf()
            })
        }
    }

    companion object {
        val shared = CharacterManager()
    }

}