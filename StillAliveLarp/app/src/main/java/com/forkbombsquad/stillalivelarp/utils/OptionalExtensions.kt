package com.forkbombsquad.stillalivelarp.utils

import java.util.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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