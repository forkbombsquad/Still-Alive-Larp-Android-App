package com.forkbombsquad.stillalivelarp.utils
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.LocalDataManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun globalPrint(message: String) {
    if (isUnitTesting) {
        globalUnitTestPrint("FORWARD FROM REGULAR GLOBAL PRINT:\n$message")
    } else {
        if (Constants.Logging.showLogging) {
            Log.wtf("LOG", "-\n$message")
        }
    }
}

fun globalTestPrint(message: Any) {
    if (Constants.Logging.showTestLogging) {
        Log.wtf("LOG", message.toString())
    }
}

var globalLastUnitTestPrint = ""
    private set

enum class UnitTestColor(val colorCode: String) {
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    RESET_COLOR("\u001B[0m")
}

fun globalUnitTestPrint(message: Any, color: UnitTestColor = UnitTestColor.RED) {
    if (Constants.Logging.showUnitTestLogging) {
        globalLastUnitTestPrint = message.toString()
        println("${color.colorCode}UNIT-TEST-PRINT-MSG: \"$message\"${UnitTestColor.RESET_COLOR.colorCode}")
    }
}

fun globalUITestPrint(message: Any) {
    if (Constants.Logging.showUITestLogging) {
        Log.wtf("LOG", message.toString())
    }
}

fun globalGetContext(): Context? {
    return StillAliveLarpApplication.currentActivty
}

fun globalToJson(model: Any): String {
    val gson = Gson()
    return gson.toJson(model)
}

inline fun <reified T> globalFromJson(json: String): T? {
    val gson = Gson()
    return tryOptional {
        val type = object : TypeToken<T>() {}.type
        gson.fromJson<T>(json, type)
    }
}

fun <T> globalFromJson(json: String, type: Type): T? {
    val gson = Gson()
    return tryOptional {
        val obj: T? = gson.fromJson<T>(json, type)
        // Fail fast if any non-nullable property is null
        obj.ifLet {
            val kclass = it::class
            kclass.memberProperties.forEach { prop ->
                if (!prop.returnType.isMarkedNullable) {
                    @Suppress("UNCHECKED_CAST")
                    val property = prop as KProperty1<T, Any?>
                    val value = property.get(it)
                        ?: throw IllegalArgumentException(
                            "Deserialization failed: required property '${prop.name}' is null in JSON: $json"
                        )
                }
            }
        }
        obj
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

fun getFragmentOrActivityName(kClass: KClass<*>): String {
    return when {
        Fragment::class.java.isAssignableFrom(kClass.java) ->
            kClass.simpleName ?: "UnnamedFragment"
        NoStatusBarActivity::class.java.isAssignableFrom(kClass.java) ->
            kClass.simpleName ?: "UnnamedActivity"
        else -> "UnknownComponent"
    }
}
fun globalForceResetAllPlayerData() {
    DataManager.forceReset()
    LocalDataManager.clearAllLocalData()
}

fun globalStyleHtmlForRulebook(html: String): String {
    return html
        .replaceHtmlTagWithTag("skill", "b")
        .replaceHtmlTagWithTagAndInnerValue("combat", "font", "color='#910016'")
        .replaceHtmlTagWithTagAndInnerValue("profession", "font", "color='#0D8017'")
        .replaceHtmlTagWithTagAndInnerValue("talent", "font", "color='#007AFF'")
        .replaceHtmlTagWithTag("item", "i")
        .replaceHtmlTagWithTag("condition", "u")
}

val isUnitTesting: Boolean by lazy {
    try {
        Class.forName("org.junit.jupiter.api.Test")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}