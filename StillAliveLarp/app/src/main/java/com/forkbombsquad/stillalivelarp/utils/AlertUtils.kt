package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.content.DialogInterface
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

    }
}