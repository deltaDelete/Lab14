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
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wajahatkarim3.easyvalidation.core.rules.BaseRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import ru.deltadelete.lab14.R
import ru.deltadelete.lab14.SecondFragment
import ru.deltadelete.lab14.api.Common
import ru.deltadelete.lab14.api.RegisterBody
import ru.deltadelete.lab14.api.User
import ru.deltadelete.lab14.databinding.RegisterSheetContentBinding
import ru.deltadelete.lab14.utils.AgeValidator
import ru.deltadelete.lab14.utils.DATETIME_FORMAT
import ru.deltadelete.lab14.utils.DATE_FORMAT
import ru.deltadelete.lab14.utils.PHONE_REGEX
import ru.deltadelete.lab14.utils.addValidationToList
import ru.deltadelete.lab14.utils.formatInsertAt
import ru.deltadelete.lab14.utils.formatStartsWith
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


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
        initOnRegisterClick()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    private val calendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.YEAR, -18)
    }

    private val calendarConstraints =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(calendar.timeInMillis))
            .build()


    fun navigateLoggedIn(user: User) {
        val json = Gson().toJson(user)
        findNavController().navigate(
            R.id.action_registerBottomSheet_to_SecondFragment,
            bundleOf(
                SecondFragment.ARG_USER to json
            )
        )
    }

    val validators = emptyList<Pair<EditText?, TextWatcher?>>().toMutableList()

    private fun setupInputFilters() {
        binding.phoneInput.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.phoneInput.formatStartsWith("+")
        // Автодобавление слешей
        binding.birthdateInput.formatInsertAt(2, "/")
        binding.birthdateInput.formatInsertAt(5, "/")

        binding.emailInputLayout.errorIconDrawable = null
        binding.passwordInputLayout.errorIconDrawable = null
        binding.passwordConfirmInputLayout.errorIconDrawable = null
        binding.emailInputLayout.errorIconDrawable = null
        binding.birthdateInputLayout.errorIconDrawable = null
        binding.phoneInputLayout.errorIconDrawable = null

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
                .minLength(8, resources.getQuantityString(R.plurals.minimal_length_is, 8, 8))
        }

        binding.passwordConfirmInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .minLength(8, resources.getQuantityString(R.plurals.minimal_length_is, 8, 8))
                .textEqualTo(
                    binding.passwordInput.text.toString(),
                    getString(R.string.unmatching_passwords)
                )
        }

        binding.lastnameInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
        }

        binding.firstnameInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
        }

        binding.birthdateInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field)).regex(
                DATE_FORMAT,
                getString(R.string.invalid_format)
            )
                .addRule(
                    AgeValidator(
                        18,
                        resources.getQuantityString(R.plurals.age_at_least, 18, 18)
                    )
                )
        }
    }

    private fun initOnRegisterClick() {
        binding.confirmButton.setOnClickListener {
            validators.forEach { (editText, textWatcher) ->
                textWatcher?.afterTextChanged(editText?.editableText)
            }
            if (checkErrors()) {
                return@setOnClickListener
            }
            binding.apply {
                this@RegisterBottomSheet.viewModel.register(
                    RegisterBody(
                        lastnameInput.text.toString(),
                        firstnameInput.text.toString(),
                        SimpleDateFormat(
                            DATETIME_FORMAT,
                            Locale.getDefault()
                        ).parse(birthdateInput.text.toString()),
                        emailInput.text.toString(),
                        passwordInput.text.toString(),
                        passwordConfirmInput.text.toString()
                    )
                ) {
                    if (it != null) {
                        this@RegisterBottomSheet.navigateLoggedIn(it)
                    }
                }
            }
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
    }
}

class RegisterViewModel : ViewModel() {

    val email: MutableLiveData<String> = MutableLiveData("")
    val birthdate: MutableLiveData<Date> = MutableLiveData(Date())

    fun register(registerBody: RegisterBody, callback: (User?) -> Unit) {
        // TODO: Реальная регистрация не работает со стороны сервера
//        viewModelScope.launch {
//            Common.loginService.registerSuspend(
//                registerBody
//            ).apply {
//                val body = body()
//                if (body == null || !isSuccessful) {
//                    callback(
//                        null
//                    )
//                }
//                callback(body)
//            }
//        }
        try {
            callback(
                User(
                    UUID.randomUUID(),
                    registerBody.firstName,
                    registerBody.lastName,
                    registerBody.birthDate,
                    Date(),
                    0,
                    registerBody.email,
                    registerBody.email.uppercase(Locale.getDefault()),
                    false
                )
            )
        } catch (e: Exception) {
            callback(null)
        }
    }
}