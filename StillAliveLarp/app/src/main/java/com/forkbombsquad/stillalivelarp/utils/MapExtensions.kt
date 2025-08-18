package com.forkbombsquad.stillalivelarp.utils

fun <K, V> MutableMap<K, MutableList<V>>.addCreateListIfNecessary(key: K, value: V) {
    this.getOrPut(key) { mutableListOf() }.add(value)
}