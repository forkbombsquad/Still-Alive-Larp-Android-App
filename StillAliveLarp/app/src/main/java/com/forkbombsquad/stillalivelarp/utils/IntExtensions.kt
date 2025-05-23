package com.forkbombsquad.stillalivelarp.utils

import kotlin.math.max

fun Int.addMinOne(value: Int): Int {
    val v = this + value
    return max(v, 1)
}

fun Int.equalsAnyOf(array: IntArray): Boolean {
    array.forEach {
        if (it == this) {
            return true
        }
    }
    return false
}

fun Int.equalsAnyOf(array: Array<Int>): Boolean {
    array.forEach {
        if (it == this) {
            return true
        }
    }
    return false
}

fun Int.equalsAnyOf(list: List<Int>): Boolean {
    return list.contains(this)
}

