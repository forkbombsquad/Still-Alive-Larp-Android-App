package com.forkbombsquad.stillalivelarp.utils
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.forkbombsquad.stillalivelarp.services.managers.*
import com.google.gson.Gson

fun globalPrint(message: String) {
    if (Constants.Logging.showLogging) {
        Log.wtf("LOG", "-\n$message")
    }
}

fun globalGetContext(): Context {
    return StillAliveLarpApplication.context
}

fun globalToJson(model: Any): String {
    val gson = Gson()
    return gson.toJson(model)
}

inline fun <reified T> globalFromJson(json: String): T? {
    val gson = Gson()
    return tryOptional {
        return gson.fromJson(json, T::class.java)
    }
}

inline fun <T> tryOptional(expression: () -> T): T? {
    return try {
        expression()
    } catch (ex: Throwable) {
        null
    }
}

fun globalCopyToClipboard(context: Context, view: KeyValueView) {
    globalCopyToClipboard(context, view.valueView.text.toString())
}

fun globalCopyToClipboard(context: Context, view: TextView) {
    globalCopyToClipboard(context, view.text.toString())
}

fun globalCopyToClipboard(context: Context, string: String) {
    val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", string)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
}

fun globalForceResetAllPlayerData(context: Context) {
    DataManager.forceReset()
    SharedPrefsManager.shared.clearAll(context)
    UserAndPassManager.shared.clear(context)
    PlayerManager.shared.forceReset()
    CharacterManager.shared.forceReset()
}