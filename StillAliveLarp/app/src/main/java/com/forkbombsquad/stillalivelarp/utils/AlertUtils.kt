package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.content.DialogInterface
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.ErrorModel

data class AlertButton(val text: String, val onClick: DialogInterface.OnClickListener, val buttonType: ButtonType)

enum class ButtonType {
    POSITIVE, NEUTRAL, NEGATIVE
}

class AlertUtils {
    companion object {

        fun displaySomethingWentWrong(context: Context) {
            displayOkMessage(context, "Ope", "Something went wrong - likely a server error. Please try again.\nIf this continues to happen, please contact Rydge Craker")
        }

        fun displayValidationError(context: Context, errors: String) {
            displayOkMessage(context, "Validation Error(s)", errors)
        }

        fun displayError(context: Context, message: String) {
            displayOkMessage(context, "Error", message)
        }

        fun displayError(context: Context, statusCode: Int, error: ErrorModel) {
            displayOkMessage(context, "Error - $statusCode", error.detail)
        }

        fun displayError(context: Context, message: String, onClick: DialogInterface.OnClickListener) {
            displayOkMessage(context, "Error", message, onClick)
        }

        fun displaySuccessMessage(context: Context, message: String, onClick: DialogInterface.OnClickListener? = null) {
            displayMessage(context, "Success", message, arrayOf(AlertButton(context.getString(android.R.string.ok), onClick ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.POSITIVE)))
        }

        fun displayOkMessage(context: Context, title: String, message: String, onClick: DialogInterface.OnClickListener? = null) {
            displayMessage(context, title, message, arrayOf(AlertButton(context.getString(android.R.string.ok), onClick ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.POSITIVE)))
        }

        fun displayOkCancelMessage(context: Context, title: String, message: String, onClickOk: DialogInterface.OnClickListener? = null, onClickCancel: DialogInterface.OnClickListener? = null) {
            displayMessage(context, title, message, arrayOf(AlertButton(context.getString(android.R.string.ok), onClickOk ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.POSITIVE), AlertButton(context.getString(android.R.string.cancel), onClickCancel ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.NEGATIVE)))
        }

        fun displayYesNoMessage(context: Context, title: String, message: String, onClickYes: DialogInterface.OnClickListener? = null, onClickNo: DialogInterface.OnClickListener? = null) {
            displayMessage(context, title, message, arrayOf(AlertButton(context.getString(android.R.string.yes), onClickYes ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.POSITIVE), AlertButton(context.getString(android.R.string.no), onClickNo ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.NEGATIVE)))
        }

        fun displayDeleteAccountCancelMessage(context: Context, onClickOk: DialogInterface.OnClickListener? = null, onClickCancel: DialogInterface.OnClickListener? = null) {
            displayMessage(context, "Are You Sure?", "Once your account is deleted, it will be gone forever and CAN NOT be recovered.", arrayOf(AlertButton("Delete Account", onClickOk ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.NEGATIVE), AlertButton(context.getString(android.R.string.cancel), onClickCancel ?: DialogInterface.OnClickListener { _, _ -> }, ButtonType.NEUTRAL)))
        }

        /**
         * Only works for one type of each button
         */
        fun displayMessage(context: Context, title: String, message: String, buttons: Array<AlertButton>) {
            StillAliveLarpApplication.activity.runOnUiThread {
                val alert = AlertDialog.Builder(context)
                alert.setTitle(title)
                alert.setMessage(message)
                for (button in buttons) {
                    when (button.buttonType) {
                        ButtonType.POSITIVE -> alert.setPositiveButton(button.text, button.onClick)
                        ButtonType.NEUTRAL -> alert.setNeutralButton(button.text, button.onClick)
                        ButtonType.NEGATIVE -> alert.setNegativeButton(button.text, button.onClick)
                    }
                }
                alert.show()
            }
        }

        fun displayChoiceMessage(context: Context, title: String, choices: Array<String>, response: (index: Int) -> Unit) {
            StillAliveLarpApplication.activity.runOnUiThread {
                val alert = AlertDialog.Builder(context)
                var selectedIndex = 0
                alert.setTitle(title)
                alert.setSingleChoiceItems(choices, 0) { _, index ->
                    selectedIndex = index
                }
                alert.setPositiveButton("Ok") { _, _ ->
                    response(selectedIndex)
                }
                alert.setNegativeButton("Cancel") { _, _ ->
                    response(-1)
                }
                alert.show()
            }
        }

        fun displayMessageWithTextField(context: Context, title: String, response: (text: String) -> Unit) {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 12, 12, 12)
            }

            val editText = EditText(context).apply {
                hint = "Enter a Name for your Planned Character"
            }

            layout.addView(editText)

            StillAliveLarpApplication.activity.runOnUiThread {
                val alert = AlertDialog.Builder(context)
                alert.setTitle(title)
                alert.setView(layout)
                alert.setPositiveButton("Ok") { _, _ ->
                    response(editText.text.toString())
                }
                alert.show()
            }
        }

    }
}