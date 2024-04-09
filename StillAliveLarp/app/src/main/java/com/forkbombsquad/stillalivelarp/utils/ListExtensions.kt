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