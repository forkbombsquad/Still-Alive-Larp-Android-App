package com.forkbombsquad.stillalivelarp.utils

import android.widget.EditText

data class ValidationResult(var hasError: Boolean, private var errorMessages: String?) {
    fun addErrorMessage(errorMessage: String) {
        if (errorMessages == null) {
            hasError = true
            errorMessages = ""
        } else {
            errorMessages += "\n"
        }
        errorMessages += errorMessage
    }

    fun getErrorMessages(): String {
        return errorMessages ?: ""
    }
}

data class ValidationGroup(val editText: EditText, val validationType: ValidationType) {
    constructor(keyValueTextField: KeyValueTextFieldView, validationType: ValidationType): this(keyValueTextField.valueTextField, validationType)
}

enum class ValidationType(val subtypes: Array<ValidationSubtype>) {
    FULL_NAME(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_FIVE_CHARACTERS_LONG, ValidationSubtype.NEEDS_SPACE)),
    EMAIL(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.EMAIL_STYLE, ValidationSubtype.AT_LEAST_EIGHT_CHARACTERS_LONG)),
    PASSWORD(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_EIGHT_CHARACTERS_LONG)),
    SECURITY_CODE(arrayOf(ValidationSubtype.NOT_EMPTY)),
    POSTAL_CODE(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.BETWEEN_5_AND_7_CHARS_LONG)),
    MESSAGE(arrayOf(ValidationSubtype.NOT_EMPTY)),
    INFECTION(arrayOf(ValidationSubtype.ALL_NUMBERS, ValidationSubtype.BETWEEN_0_AND_100)),
    BULLETS(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    MEGAS(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    RIVALS(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    ROCKETS(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    BULLET_CASINGS(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    CLOTH(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    WOOD(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    METAL(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    TECH(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    MEDICAL(arrayOf(ValidationSubtype.ALL_NUMBERS)),
    ANNOUNCEMENT_TITLE(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_FIVE_CHARACTERS_LONG)),
    ANNOUNCEMENT_MESSAGE(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_FIVE_CHARACTERS_LONG)),
    INTRIGUE(arrayOf(ValidationSubtype.NOT_EMPTY)),
    TITLE(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_FIVE_CHARACTERS_LONG)),
    DATE(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.EXACTLY_10_CHARACTERS_LONG, ValidationSubtype.DATE_FORMATTED)),
    START_TIME(arrayOf(ValidationSubtype.NOT_EMPTY)),
    END_TIME(arrayOf(ValidationSubtype.NOT_EMPTY)),
    DESCRIPTION(arrayOf(ValidationSubtype.NOT_EMPTY)),
    PRIMARY_WEAPON_NAME(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_TWO_CHARACTERS_LONG)),
    PRIMARY_WEAPON_AMMO(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_TWO_CHARACTERS_LONG)),
    GEAR_TYPE(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_TWO_CHARACTERS_LONG)),
    GEAR_NAME(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_TWO_CHARACTERS_LONG)),
    GEAR_DESCRIPTION(arrayOf(ValidationSubtype.NOT_EMPTY, ValidationSubtype.AT_LEAST_TWO_CHARACTERS_LONG))

}

enum class ValidationSubtype {
    NOT_EMPTY,
    NEEDS_SPACE,
    AT_LEAST_FIVE_CHARACTERS_LONG,
    EMAIL_STYLE,
    AT_LEAST_EIGHT_CHARACTERS_LONG,
    BETWEEN_5_AND_7_CHARS_LONG,
    ALL_NUMBERS,
    BETWEEN_0_AND_100,
    EXACTLY_10_CHARACTERS_LONG,
    DATE_FORMATTED,
    AT_LEAST_TWO_CHARACTERS_LONG
}

class Validator {
    companion object {

        fun validateMultiple(validationGroups: Array<ValidationGroup>): ValidationResult {
            var validationResult = ValidationResult(false, null)
            for (validationGroup in validationGroups) {
                val vr = validate(validationGroup)
                if (vr.hasError) {
                    validationResult.addErrorMessage(vr.getErrorMessages())
                }
            }
            return validationResult
        }

        fun validate(validationGroup: ValidationGroup): ValidationResult {
            return validate(validationGroup.editText, validationGroup.validationType)
        }

        fun validate(editText: EditText, validationType: ValidationType): ValidationResult {
            var validationResult = ValidationResult(false, null)
            doValidation(editText.text.toString(), validationType).ifLet {
                validationResult.addErrorMessage(it)
            }
            return validationResult
        }

        private fun doValidation(text: String, validationType: ValidationType): String? {
            var error = ""
            val name = validationType.name.lowercase().replace("_", " ").capitalized()
            for (valSubtype in validationType.subtypes) {
                when(valSubtype) {
                    ValidationSubtype.NOT_EMPTY -> {
                        if (text.isEmpty()) {
                            error = addToError(error, "$name must not be empty")
                        }
                    }
                    ValidationSubtype.NEEDS_SPACE -> {
                        if (!text.contains(" ")) {
                            error = addToError(error,  "$name must contain a space")
                        }
                    }
                    ValidationSubtype.AT_LEAST_FIVE_CHARACTERS_LONG -> {
                        if (text.count() < 5) {
                            error = addToError(error,  "$name must be at least 5 characters long")
                        }
                    }
                    ValidationSubtype.EMAIL_STYLE -> {
                        if (!text.contains("@")) {
                            error = addToError(error,  "$name must contain @")
                        }
                        if (!text.contains(".")) {
                            error = addToError(error,  "$name must contain .")
                        }
                    }
                    ValidationSubtype.AT_LEAST_EIGHT_CHARACTERS_LONG -> {
                        if (text.count() < 8) {
                            error = addToError(error,  "$name must be at least 8 characters long")
                        }
                    }
                    ValidationSubtype.BETWEEN_5_AND_7_CHARS_LONG -> {
                        if (text.count() < 5 || text.count() > 7) {
                            error = addToError(error, "$name must be between 5 and 7 characters long")
                        }
                    }
                    ValidationSubtype.ALL_NUMBERS -> {
                        if (text.toIntOrNull() == null) {
                            error = addToError(error, "$name must consist of only numbers!")
                        }
                    }
                    ValidationSubtype.BETWEEN_0_AND_100 -> {
                        if (text.toInt() < 0 || text.toInt() > 100) {
                            error = addToError(error, "$name must be between 0 and 100")
                        }
                    }
                    ValidationSubtype.EXACTLY_10_CHARACTERS_LONG -> {
                        if (text.count() != 10) {
                            error = addToError(error, "$name must be exactly 10 characters long")
                        }
                    }
                    ValidationSubtype.DATE_FORMATTED -> {
                        val spl = text.split("/")
                        if (spl.count() != 3) {
                            error = addToError(error, "$name must be formatted exactly as yyyy/MM/dd, i.e. 2023/23/01")
                        } else {
                            if (spl[0].count() != 4 || spl[1].count() != 2 || spl[2].count() != 2) {
                                error = addToError(error, "$name must be formatted exactly as yyyy/MM/dd, i.e. 2023/23/01")
                            } else {
                                if (spl[0].toIntOrNull() == null || spl[1].toIntOrNull() == null || spl[2].toIntOrNull() == null) {
                                    error = addToError(error, "$name must be formatted exactly as yyyy/MM/dd, i.e. 2023/23/01")
                                }
                            }
                        }
                    }
                    ValidationSubtype.AT_LEAST_TWO_CHARACTERS_LONG -> {
                        if (text.count() < 2) {
                            error = addToError(error,  "$name must be at least 2 characters long")
                        }
                    }
                }
            }
            return error.ifEmpty { null }
        }

        private fun addToError(error: String, newError: String): String {
            return if (error.isEmpty()) newError else error + "\n" + newError
        }
    }
}