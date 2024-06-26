package com.forkbombsquad.stillalivelarp.utils

import kotlin.math.max

fun Boolean.ternary(value: String, otherwise: String): String {
    if (this) {
        return value
    }
    return otherwise
}

fun Boolean.ternary(value: Int, otherwise: Int): Int {
    if (this) {
        return value
    }
    return otherwise
}

fun <T> Boolean.ternary(value: T?, otherwise: T?): T? {
    if (this) {
        return value
    }
    return otherwise
}