package ru.deltadelete.lab14.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.wajahatkarim3.easyvalidation.core.Validator
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator

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
        if (it.length >= where) {
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
    validators: MutableList<Pair<Editable?, TextWatcher?>>,
    validatorBuilder: Validator.() -> Validator
) {
    validators.add(editText?.editableText to addValidation(validatorBuilder))
}