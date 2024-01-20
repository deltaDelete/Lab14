package ru.deltadelete.lab14.utils

import android.icu.text.SimpleDateFormat
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.textfield.TextInputLayout
import com.wajahatkarim3.easyvalidation.core.Validator
import com.wajahatkarim3.easyvalidation.core.rules.BaseRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import java.text.ParseException
import java.util.Calendar
import java.util.Locale

fun EditText.validRegex(pattern: String, errorMessage: String? = null): Boolean {
    return validator().regex("", errorMessage)
        .check()
}

fun EditText.validRegex(
    pattern: String,
    errorMessage: String? = null,
    callback: (String) -> Unit
): Boolean {
    return validator().regex("", errorMessage)
        .addErrorCallback {
            callback.invoke(it)
        }
        .check()
}

fun EditText.formatStartsWith(prefix: CharSequence) {
    doAfterTextChanged {
        it?.startsWith(prefix)?.ifFalse {
            it.insert(0, prefix)
        }
    }
}

fun EditText.formatInsertAt(where: Int, value: CharSequence) {
    doAfterTextChanged {
        if (it == null) {
            return@doAfterTextChanged
        }
        if (it.startsWith(value, where)) {
            return@doAfterTextChanged
        }
        if (it.length > where) {
            it.insert(where, value)
        }
    }
}

fun EditText.addValidation(validatorBuilder: Validator.() -> Validator) {
    doAfterTextChanged {
        Validator(it.toString()).validatorBuilder()
            .addErrorCallback { error ->
                this.error = error
            }
            .addSuccessCallback {
                this.error = null
            }
            .check()
    }
}

fun TextInputLayout.addValidation(validatorBuilder: Validator.() -> Validator): TextWatcher? {
    return editText?.doAfterTextChanged {
        Validator(it.toString()).validatorBuilder()
            .addErrorCallback { error ->
                if (error.isBlank())
                    return@addErrorCallback
                this.error = error
            }
            .addSuccessCallback {
                this.error = null
            }.check()
    }
}

fun Boolean.ifTrue(block: () -> Unit) {
    if (this) {
        block.invoke()
    }
}

fun Boolean.ifFalse(block: () -> Unit) {
    if (this) {
        return
    }
    block.invoke()
}

fun TextInputLayout.addValidationToList(
    validators: MutableList<Pair<EditText?, TextWatcher?>>,
    validatorBuilder: Validator.() -> Validator
) {
    validators.add(editText to addValidation(validatorBuilder))
}

class AgeValidator(private val atLeast: Int, private var errorMessage: String? = null) : BaseRule {
    override fun getErrorMessage(): String {
        return errorMessage ?: ""
    }

    override fun setError(msg: String) {
        errorMessage = msg
    }

    override fun validate(text: String): Boolean {
        if (text.isBlank()) {
            return false
        }
        return try {
            val time = dateFormat.parse(text).time
            calendarConstraints.dateValidator.isValid(time)
        } catch(e: ParseException) {
            false
        }
    }

    private val calendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.YEAR, -atLeast)
    }

    private val calendarConstraints =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(calendar.timeInMillis))
            .build()

    private val dateFormat: SimpleDateFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
}

const val DATETIME_FORMAT = "dd/MM/yyyy"
val PHONE_REGEX =
    "^\\+?\\d{1,3}\\s?\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}\$"
val PHONE_REGEX_GROUPS =
    "^(\\+?(\\d{1,3}))\\s?(\\d{3})[\\s-]?(\\d{3})[\\s-]?(\\d{2})[\\s-]?(\\d{2})\$"
val DIGIT_MINUS_PLUS_OR_SPACE: Regex = "[\\d\\s-]?".toRegex()
val EMAIL_REGEX = androidx.core.util.PatternsCompat.EMAIL_ADDRESS.toRegex()
val DATE_FORMAT = "\\d{2}/(?:0[0-9]|1[0-2])/(?:\\d{2}|\\d{4})"