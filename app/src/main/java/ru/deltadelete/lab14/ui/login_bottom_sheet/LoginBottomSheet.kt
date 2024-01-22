package ru.deltadelete.lab14.ui.login_bottom_sheet

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import ru.deltadelete.lab14.MainActivity
import ru.deltadelete.lab14.R
import ru.deltadelete.lab14.SecondFragment
import ru.deltadelete.lab14.api.Common
import ru.deltadelete.lab14.api.LoginBody
import ru.deltadelete.lab14.api.User
import ru.deltadelete.lab14.databinding.LoginSheetContentBinding
import ru.deltadelete.lab14.utils.addValidationToList

class LoginBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: LoginSheetContentBinding

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var progressIndicatorDrawable: IndeterminateDrawable<CircularProgressIndicatorSpec>
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginSheetContentBinding.inflate(inflater, container, false)

        (dialog as BottomSheetDialog).behavior.apply {
            skipCollapsed = true
        }

        sharedPreferences = requireActivity().getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
        handleRemembered()

        val spec = CircularProgressIndicatorSpec(
            requireContext(),
            null,
            0,
            com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
        )

        progressIndicatorDrawable = IndeterminateDrawable.createCircularDrawable(
            requireContext(), spec
        )

        setupInputFilters()
        initOnLoginClick()

        return binding.root
    }

    private fun handleRemembered() {
        val rememberMe = sharedPreferences.getBoolean("REMEMBER_ME", false)
        binding.rememberMeCheckbox.apply {
            isChecked = rememberMe
            setOnCheckedChangeListener { _, isChecked ->
                val edit = sharedPreferences.edit()
                edit.putBoolean("REMEMBER_ME", isChecked)
                edit.apply()
            }
        }

        if (rememberMe) {
            sharedPreferences.getString("USER", null)?.let {
                val user = gson.fromJson(it, User::class.java)
                binding.loginInput.setText(user.email)
            }
        }
    }

    private val validators = emptyList<Pair<EditText?, TextWatcher?>>().toMutableList()
    private fun setupInputFilters() {
        binding.loginInputLayout.errorIconDrawable = null
        binding.passwordInputLayout.errorIconDrawable = null

        binding.loginInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .validEmail(getString(R.string.invalid_email))
        }

        binding.passwordInputLayout.addValidationToList(validators) {
            nonEmpty(getString(R.string.required_field))
                .minLength(8, resources.getQuantityString(R.plurals.minimal_length_is, 8, 8))
        }
    }

    private fun initOnLoginClick() {
        binding.confirmButton.setOnClickListener {
            binding.confirmButton.icon = progressIndicatorDrawable
            validators.forEach { (editText, textWatcher) ->
                textWatcher?.afterTextChanged(editText?.editableText)
            }
            if (checkErrors()) {
                binding.confirmButton.icon = null
                return@setOnClickListener
            }
            binding.apply {
                this@LoginBottomSheet.viewModel.login(
                    LoginBody(
                        loginInput.text.toString(),
                        passwordInput.text.toString()
                    )
                ) {
                    binding.confirmButton.icon = null
                    if (it == null) {
                        return@login
                    }
                    if (binding.rememberMeCheckbox.isChecked) {
                        val edit = sharedPreferences.edit()
                        edit.putString("LOGIN", it.email)
                        edit.putString("USER", gson.toJson(it))
                        edit.apply()
                    }
                    this@LoginBottomSheet.navigateLoggedIn(it)
                }
            }
        }
    }

    private fun navigateLoggedIn(user: User) {
        val json = Gson().toJson(user)
        val bundle = Bundle()
        bundle.putString(SecondFragment.ARG_USER, json)
        findNavController().navigate(
            R.id.action_loginBottomSheet_to_SecondFragment,
            bundle
        )
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
        const val TAG = "LoginBottomSheet"
    }
}

class LoginViewModel : ViewModel() {

    fun login(
        loginBody: LoginBody,
        callback: ((User?) -> Unit)
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Common.loginService.login(
                loginBody
            ).enqueue(object : retrofit2.Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val body = response.body()
                    if (body == null || response.code() != 200) {
                        callback(null)
                        return
                    }
                    callback(body)
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    callback(null)
                    Log.e("$TAG.login", "Error when handling login request", t)
                    t.printStackTrace()
                }
            })
        }
    }

    companion object {
        const val TAG = "LoginViewModel"
    }
}
