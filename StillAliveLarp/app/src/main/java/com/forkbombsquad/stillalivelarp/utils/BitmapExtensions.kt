package com.forkbombsquad.stillalivelarp.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.base64String(): String {
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 70, baos)
    val ba = baos.toByteArray()
    return Base64.encodeToString(ba, Base64.DEFAULT)
}