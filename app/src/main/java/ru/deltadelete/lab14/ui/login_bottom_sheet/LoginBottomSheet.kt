package ru.deltadelete.lab14.ui.login_bottom_sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.deltadelete.lab14.api.Common
import ru.deltadelete.lab14.api.LoginBody
import ru.deltadelete.lab14.api.User
import ru.deltadelete.lab14.databinding.LoginSheetContentBinding
import java.util.Optional

class LoginBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: LoginSheetContentBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginSheetContentBinding.inflate(inflater, container, false)

        (dialog as BottomSheetDialog).behavior.apply {
            skipCollapsed = true
        }

        binding.confirmButton.setOnClickListener {
            viewModel.login {
                dialog?.dismiss()
            }
        }

        return binding.root
    }

    companion object {
        const val TAG = "LoginBottomSheet"
    }
}

class LoginViewModel : ViewModel() {

    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")

    fun login(callback: ((User?) -> Unit)) {
        // TODO: Реальный логин
        viewModelScope.launch {
            Common.loginService.loginSuspend(
                LoginBody(
                    login.value ?: "",
                    password.value ?: ""
                )
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
