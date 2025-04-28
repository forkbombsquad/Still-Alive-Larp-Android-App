package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.content.DialogInterface
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
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

        fun displayMessageWithInputs(
            context: Context,
            title: String,
            editTextHints: List<String>,
            checkboxTexts: List<String>,
            response: (values: Map<String, String>) -> Unit
        ) {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
            }

            val editTexts = mutableListOf<EditText>()
            val checkboxes = mutableListOf<CheckBox>()

            // Create and add EditTexts
            for (hint in editTextHints) {
                val editText = EditText(context).apply {
                    this.hint = hint
                }
                editTexts.add(editText)
                layout.addView(editText)
            }

            // Create and add CheckBoxes
            for (text in checkboxTexts) {
                val checkbox = CheckBox(context).apply {
                    this.text = text
                }
                checkboxes.add(checkbox)
                layout.addView(checkbox)
            }

            StillAliveLarpApplication.activity.runOnUiThread {
                val alert = AlertDialog.Builder(context)
                alert.setTitle(title)
                alert.setView(layout)
                alert.setPositiveButton("Ok") { _, _ ->
                    val resultMap = mutableMapOf<String, String>()

                    // Collect EditText values
                    for (editText in editTexts) {
                        val key = editText.hint?.toString() ?: ""
                        val value = editText.text.toString()
                        resultMap[key] = value
                    }

                    // Collect CheckBox values
                    for (checkbox in checkboxes) {
                        val key = checkbox.text.toString()
                        val value = checkbox.isChecked.toString()
                        resultMap[key] = value
                    }

                    response(resultMap)
                }
                alert.setNegativeButton("Cancel", null)
                alert.show()
            }
        }

        fun displayMessageWithInputs(
            context: Context,
            title: String,
            editTexts: Map<String, EditText>,
            checkboxes: Map<String, CheckBox>,
            response: (values: Map<String, String>) -> Unit
        ) {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
            }

            for ((_, editText) in editTexts) {
                layout.addView(editText)
            }
            for ((_, checkbox) in checkboxes) {
                layout.addView(checkbox)
            }

            StillAliveLarpApplication.activity.runOnUiThread {
                val alert = AlertDialog.Builder(context)
                alert.setTitle(title)
                alert.setView(layout)
                alert.setPositiveButton("Ok") { _, _ ->
                    val resultMap = mutableMapOf<String, String>()

                    // Collect EditText values
                    for (editText in editTexts) {
                        resultMap[editText.key] = editText.value.text.toString()
                    }

                    // Collect CheckBox values
                    for (checkbox in checkboxes) {
                        resultMap[checkbox.key] = checkbox.value.isChecked.toString()
                    }

                    response(resultMap)
                }
                alert.setNegativeButton("Cancel", null)
                alert.show()
            }
        }

    }
}