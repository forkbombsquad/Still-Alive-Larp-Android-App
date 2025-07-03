package com.forkbombsquad.stillalivelarp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

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

fun String.toBitmap(): Bitmap {
    val imageBytes = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

fun String.replaceHtmlTag(tag: String, replaceWith: String = ""): String {
    return this.replace("<$tag>", replaceWith).replace("</$tag>", replaceWith)
}

fun String.replaceHtmlTags(tags: List<String>, replaceWith: String = ""): String {
    var replacement = this
    for (tag in tags) {
        replacement = replacement.replaceHtmlTag(tag, replaceWith)
    }
    return replacement
}

fun String.compress(): String {
    val outputStream = ByteArrayOutputStream()
    GZIPOutputStream(outputStream).use { gzipStream ->
        gzipStream.write(this.toByteArray(Charsets.UTF_8))
    }
    val compressedBytes = outputStream.toByteArray()
    return Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
}

fun String.decompress(): String {
    val compressedBytes = Base64.decode(this, Base64.NO_WRAP)
    val inputStream = GZIPInputStream(ByteArrayInputStream(compressedBytes))
    return inputStream.reader(Charsets.UTF_8).readText()
}

fun String.equalsIgnoreCase(other: String): Boolean {
    return this.lowercase() == other.lowercase()
}

fun String.capitalizeOnlyFirstLetterOfEachWord(): String =
    this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }
