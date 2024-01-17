package ru.deltadelete.lab14.ui.register_bottom_sheet

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
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
import kotlinx.coroutines.launch
import ru.deltadelete.lab14.R
import ru.deltadelete.lab14.api.Common
import ru.deltadelete.lab14.api.RegisterBody
import ru.deltadelete.lab14.api.User
import ru.deltadelete.lab14.databinding.RegisterSheetContentBinding
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

        binding.confirmButton.setOnClickListener {
            register()
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    private fun register() {
        viewModel.register(RegisterBody(
            binding.lastnameInput.text.toString(),
            binding.firstnameInput.text.toString(),
            viewModel.birthdate.value!!,
            viewModel.email.value!!,
            binding.passwordInput.text.toString(),
            binding.passwordConfirmInput.text.toString()
        )) {
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

        binding.emailInput.addTextChangedListener {
            it?.matches(EMAIL_REGEX)?.let { bool ->
                if (bool) {
                    binding.emailInputLayout.error = getString(R.string.invalid_email)
                } else {
                    binding.emailInputLayout.error = null
                }
            }
        }

        binding.root.forEach {
            if (it is TextInputLayout) {
                it.editText?.addTextChangedListener { text ->
                    if (text.isNullOrBlank()) {
                        it.error = "Заполните это поле"
                    } else {
                        it.error = null
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "RegisterBottomSheet"
        const val DATETIME_FORMAT = "dd/MM/yyyy"
        val PHONE_REGEX =
            "^(\\+?(\\d{1,3}))\\s?(\\d{3})[\\s-]?(\\d{3})[\\s-]?(\\d{2})[\\s-]?(\\d{2})\$".toRegex()
        val DIGIT_MINUS_PLUS_OR_SPACE: Regex = "[\\d\\s-]?".toRegex()
        val EMAIL_REGEX = androidx.core.util.PatternsCompat.EMAIL_ADDRESS.toRegex()
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