package ru.deltadelete.lab14.ui.register_bottom_sheet

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy
import kotlinx.coroutines.launch
import ru.deltadelete.lab14.MainActivity
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
import ru.deltadelete.lab14.utils.installOn
import java.util.Calendar
import java.util.Locale


class RegisterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: RegisterSheetContentBinding
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var progressIndicatorDrawable: IndeterminateDrawable<CircularProgressIndicatorSpec>
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterSheetContentBinding.inflate(inflater, container, false)

        sharedPreferences = requireActivity().getSharedPreferences(
            MainActivity.PREFS,
            Context.MODE_PRIVATE
        )

        (dialog as BottomSheetDialog).behavior.apply {
            skipCollapsed = true
        }
        val spec = CircularProgressIndicatorSpec(
            requireContext(),
            null,
            0,
            com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
        )

        progressIndicatorDrawable = IndeterminateDrawable.createCircularDrawable(
            requireContext(), spec
        )

        setupInputMasks()
        setupInputFilters()
        initOnRegisterClick()

        viewModel.rememberMe.observe(viewLifecycleOwner) {
            binding.rememberMeCheckbox.isChecked = it
            val edit = sharedPreferences.edit()
            edit.putBoolean("REMEMBER_ME", it)
            edit.apply()
        }

        binding.rememberMeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.rememberMe.postValue(isChecked)
        }

        return binding.root
    }

    private val calendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.YEAR, -18)
    }

    private val calendarConstraints =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(calendar.timeInMillis))
            .build()


    private fun navigateLoggedIn(user: User) {
        val json = Gson().toJson(user)
        val bundle = Bundle()
        bundle.putString(SecondFragment.ARG_USER, json)
        findNavController().navigate(
            R.id.action_registerBottomSheet_to_SecondFragment,
            bundle
        )
    }

    val validators = emptyList<Pair<EditText?, TextWatcher?>>().toMutableList()

    private fun setupInputFilters() {
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

    private fun setupInputMasks() {
        MaskedTextChangedListener.installOn(
            binding.phoneInput,
            "+[0] ([000]) [000]-[00]-[00]",
            listOf("+[00] ([000]) [000]-[00]-[00]", "+[000] ([000]) [000]-[00]-[00]"),
            AffinityCalculationStrategy.CAPACITY
        )
        { maskFilled, extractedValue, formattedValue, tailPlaceholder ->
            Log.d("$TAG.Masked", extractedValue)
            Log.d("$TAG.Masked", maskFilled.toString())
            binding.phoneInputLayout.error =
                if (!maskFilled) getString(R.string.invalid_phone_number) else null
        }

        MaskedTextChangedListener.Companion.installOn(
            binding.birthdateInput,
            "[00]{/}[00]{/}[0000]",
        ) { maskFilled, extractedValue, formattedValue, tailPlaceholder ->
            if (maskFilled) {
                binding.birthdateInputLayout.error = null
                return@installOn
            }
            binding.birthdateInputLayout.error = getString(R.string.invalid_format)
            binding.birthdateInputLayout.placeholderText = tailPlaceholder
        }
    }

    private fun initOnRegisterClick() {
        binding.confirmButton.setOnClickListener {
            binding.confirmButton.icon = progressIndicatorDrawable
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
                ) { it, already ->
                    binding.confirmButton.icon = null
                    if (already) {
                        binding.emailInputLayout.error = getString(R.string.already_registered)
                    }
                    if (it == null) {
                        return@register
                    }
                    if (viewModel.rememberMe.value == true) {
                        val edit = sharedPreferences.edit()
                        edit.putString("USER", gson.toJson(it))
                        edit.apply()
                    }

                    this@RegisterBottomSheet.navigateLoggedIn(it)
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

class RegisterViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val rememberMe = savedStateHandle.getLiveData(REMEMBER_ME_KEY, false)

    fun register(registerBody: RegisterBody, callback: (User?, alreadyRegistered: Boolean) -> Unit) {
        viewModelScope.launch {
            Common.loginService.registerSuspend(
                registerBody
            ).apply {
                val body = body()
                if (body == null) {
                    when (code()) {
                        409 -> callback(null, true)
                        else -> callback(null, false)
                    }
                }
                callback(body, false)
            }
        }
    }

    companion object {
        const val REMEMBER_ME_KEY = "RememberMe"
    }
}