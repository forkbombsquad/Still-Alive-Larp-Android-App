package com.forkbombsquad.stillalivelarp.utils
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.forkbombsquad.stillalivelarp.services.managers.CharacterManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.PlayerManager
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
import com.google.gson.Gson
import java.lang.reflect.Type

fun globalPrint(message: String) {
    if (Constants.Logging.showLogging) {
        Log.wtf("LOG", "-\n$message")
    }
}

fun globalTestPrint(message: Any) {
    if (Constants.Logging.showTestLogging) {
        Log.wtf("LOG", message.toString())
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

inline fun <reified T> globalFromJson(json: String, typeToken: Type): T? {
    val gson = Gson()
    return tryOptional {
        return gson.fromJson<T>(json, typeToken)
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
    OldDataManager.forceReset()
    SharedPrefsManager.shared.clearAll(context)
    UserAndPassManager.shared.clear(context)
    PlayerManager.shared.forceReset()
    CharacterManager.shared.forceReset()
}