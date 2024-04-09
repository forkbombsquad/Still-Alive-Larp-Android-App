package com.forkbombsquad.stillalivelarp.utils

import android.text.SpannableString
import android.text.style.UnderlineSpan
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Replacement for Kotlin's deprecated `capitalize()` function.
 */
fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

fun String.yyyyMMddtoDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
}

fun String.yyyyMMddToMonthDayYear(): String {
    if (this.isEmpty()) {
        return this
    }
    val date = this.yyyyMMddtoDate()
    return date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
}

fun String.underline(): SpannableString {
    val spanStr = SpannableString(this)
    spanStr.setSpan(UnderlineSpan(), 0, spanStr.length, 0)
    return spanStr
}

fun String.containsIgnoreCase(text: String): Boolean {
    return this.lowercase().contains(text.lowercase())
}