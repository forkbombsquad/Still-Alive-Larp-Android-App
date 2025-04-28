package com.forkbombsquad.stillalivelarp.utils

fun <T, R> T?.ifLet(let: (value: T) -> R, otherwise: () -> R): R {
    this?.let {
        return let(it)
    } ?: run {
        return otherwise()
    }
}

fun <T, R> T?.ifLet(let: (value: T) -> R): R? {
    this?.let {
        let(it)
    }
    return null
}