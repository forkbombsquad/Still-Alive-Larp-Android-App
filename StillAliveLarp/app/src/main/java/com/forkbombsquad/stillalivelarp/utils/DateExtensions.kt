package com.forkbombsquad.stillalivelarp.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.yyyyMMddFormatted(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    return this.format(formatter)
}