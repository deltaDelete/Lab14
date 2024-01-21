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
import java.lang.Exception

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
        binding.loginButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_FirstFragment_to_loginBottomSheet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.registerButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_FirstFragment_to_registerBottomSheet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.findViewById<View>(R.id.toolbar)?.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.supportActionBar?.show()
        }
        super.onDestroyView()
    }
}

