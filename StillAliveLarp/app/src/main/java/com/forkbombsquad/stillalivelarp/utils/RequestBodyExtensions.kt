package com.forkbombsquad.stillalivelarp.utils

import okhttp3.Request
import okio.Buffer

fun Request.bodyToString(): String? {
    val copy = this.newBuilder().build()
    val buffer = Buffer()
    var string: String? = null
    copy.body().ifLet {
        it.writeTo(buffer)
        string = buffer.readUtf8()
    }
    buffer.close()
    return string
}