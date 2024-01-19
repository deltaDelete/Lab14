package ru.deltadelete.lab14.ui.register_bottom_sheet

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import ru.deltadelete.lab14.R
import ru.deltadelete.lab14.api.Common
import ru.deltadelete.lab14.api.RegisterBody
import ru.deltadelete.lab14.api.User
import ru.deltadelete.lab14.databinding.RegisterSheetContentBinding
import ru.deltadelete.lab14.utils.addValidationToList
import ru.deltadelete.lab14.utils.formatInsertAt
import ru.deltadelete.lab14.utils.formatStartsWith
import java.util.Calendar
import java.util.Date


class RegisterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: RegisterSheetContentBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterSheetContentBinding.inflate(inflater, container, false)

        (dialog as BottomSheetDialog).behavior.apply {
            skipCollapsed = true
        }

        setupInputFilters()

//        binding.confirmButton.setOnClickListener {
//            register()
//        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    private fun register() {
        viewModel.register(
            RegisterBody(
                binding.lastnameInput.text.toString(),
                binding.firstnameInput.text.toString(),
                viewModel.birthdate.value!!,
                viewModel.email.value!!,
                binding.passwordInput.text.toString(),
                binding.passwordConfirmInput.text.toString()
            )
        ) {
            Log.d(TAG, it.toString())
            dialog?.dismiss()
        }
    }

    private val calendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.YEAR, -18)
    }

    private val calendarConstraints =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(calendar.timeInMillis))
            .build()

    private fun setupInputFilters() {
        binding.phoneInput.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.phoneInput.formatStartsWith("+")
        // TODO: Проверка есть ли 18
        // Автодобавление слешей
        binding.birthdateInput.formatInsertAt(2, "/")
        binding.birthdateInput.formatInsertAt(5, "/")
//        binding.birthdateInput.doAfterTextChanged {
//            if (it == null) {
//                return@doAfterTextChanged
//            }
//            it.forEachIndexed { index, c ->
//                if (index != 2 && index != 5 && c == '/') {
//                    it.delete(index, index + 1)
//                }
//            }
//        }

        binding.birthdateInputLayout.setEndIconOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(calendarConstraints)
                .setSelection(calendar.timeInMillis)
                .build().apply {
                    addOnPositiveButtonClickListener {
                        binding.birthdateInput.setText(
                            DateFormat.format(DATETIME_FORMAT, it)
                        )
                    }
                }.show(parentFragmentManager, "$TAG.DATE_PICKER")
        }

        val validators = emptyList<Pair<Editable?, TextWatcher?>>().toMutableList()

        binding.phoneInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .regex(PHONE_REGEX, getString(R.string.invalid_phone_number))
        }

        binding.emailInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .validEmail(getString(R.string.invalid_email))
        }

        binding.passwordInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .greaterThanOrEqual(8, resources.getQuantityString(R.plurals.minimal_length_is, 8))
        }

        binding.passwordConfirmInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .greaterThanOrEqual(8, resources.getQuantityString(R.plurals.minimal_length_is, 8))
                .textEqualTo(binding.passwordInput.text.toString(),
                    getString(R.string.unmatching_passwords))
        }

        binding.lastnameInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
        }

        binding.firstnameInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
        }

        binding.birthdateInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field)).
            regex(DATE_FORMAT, getString(R.string.invalid_format))
        }

        binding.confirmButton.setOnClickListener {
            validators.forEach { (editable, textWatcher) ->
                textWatcher?.afterTextChanged(editable)
            }
            if (checkErrors()) {
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "Круто", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkErrors(): Boolean {
        binding.root.forEach {
            if (it is TextInputLayout) {
                if (it.error != null) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        const val TAG = "RegisterBottomSheet"
        const val DATETIME_FORMAT = "dd/MM/yyyy"
        val PHONE_REGEX =
            "^\\+?\\d{1,3}\\s?\\d{3}[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}\$"
        val PHONE_REGEX_GROUPS =
            "^(\\+?(\\d{1,3}))\\s?(\\d{3})[\\s-]?(\\d{3})[\\s-]?(\\d{2})[\\s-]?(\\d{2})\$"
        val DIGIT_MINUS_PLUS_OR_SPACE: Regex = "[\\d\\s-]?".toRegex()
        val EMAIL_REGEX = androidx.core.util.PatternsCompat.EMAIL_ADDRESS.toRegex()
        val DATE_FORMAT = "\\d{2}/(?:0[0-9]|1[0-2])/(?:\\d{2}|\\d{4})"
    }
}

class RegisterViewModel : ViewModel() {

    val email: MutableLiveData<String> = MutableLiveData("")
    val birthdate: MutableLiveData<Date> = MutableLiveData(Date())

    fun register(registerBody: RegisterBody, callback: (User?) -> Unit) {
        // TODO: Реальная регистрация
        viewModelScope.launch {
            Common.loginService.register(
                registerBody
            ).apply {
                val body = body()
                if (body == null || !isSuccessful) {
                    callback(
                        null
                    )
                }
                callback(body)
            }
        }
    }
}