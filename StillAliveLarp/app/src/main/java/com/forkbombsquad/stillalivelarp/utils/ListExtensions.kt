package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.models.*

@JvmName("alphabetizedPlayerModel")
fun List<PlayerModel>.alphabetized(): List<PlayerModel> {
    return this.sortedWith(compareBy { it.fullName })
}

@JvmName("alphabetizedCharacterModel")
fun List<CharacterModel>.alphabetized(): List<CharacterModel> {
    return this.sortedWith(compareBy { it.fullName })
}

@JvmName("alphabetizedFullCharacterModel")
fun List<FullCharacterModel>.alphabetized(): List<FullCharacterModel> {
    return this.sortedWith(compareBy { it.fullName })
}

fun List<EventModel>.inChronologicalOrder(): List<EventModel> {
    return this.sortedBy { it.date.yyyyMMddtoDate() }.filter { it.isToday() || it.isInFuture() }
}

@JvmName("alphabetizedFullSkillsModel")
fun List<FullSkillModel>.alphabetized(): List<FullSkillModel> {
    return this.sortedWith(compareBy { it.name })
}

data class PreregNumbers(val premium: Int, val premiumNpc: Int, val basic: Int, val basicNpc: Int, val free: Int, val notAttending: Int)

fun Array<EventPreregModel>.getRegNumbers(): PreregNumbers {
    var prem = 0
    var premNpc = 0
    var basic = 0
    var basicNpc = 0
    var free = 0
    var notAttending = 0
    for (prereg in this) {
        val isNpc = prereg.getCharId() == null
        when(prereg.eventRegType()) {
            EventRegType.PREMIUM -> {
                prem += 1
                if (isNpc) {
                    premNpc += 1
                }
            }
            EventRegType.BASIC -> {
                basic += 1
                if (isNpc) {
                    basicNpc += 1
                }
            }
            EventRegType.FREE -> {
                free += 1
            }
            EventRegType.NOT_PREREGED -> notAttending += 1
        }
    }
    return PreregNumbers(prem, premNpc, basic, basicNpc, free, notAttending)
}