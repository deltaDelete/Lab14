package ru.deltadelete.lab14

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.deltadelete.lab14.databinding.FragmentFirstBinding
import ru.deltadelete.lab14.ui.login_bottom_sheet.LoginBottomSheet

class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.supportActionBar?.hide()
        }
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_loginBottomSheet)
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_registerBottomSheet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

