package ru.deltadelete.lab14

import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHostHelper
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuProvider
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.deltadelete.lab14.api.User
import ru.deltadelete.lab14.databinding.FragmentSecondBinding
import java.util.Locale

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        arguments?.getString(ARG_USER)?.let {
            val gson = Gson()
            val user = gson.fromJson(it, User::class.java)
            user?.let { us ->
                binding.lastnameText.text = us.lastName
                binding.firstnameText.text = us.firstName
                binding.ratingText.text = getString(R.string.rating, us.rating)
                binding.birthdateText.text = getString(R.string.birthday, dateFormat.format(us.birthDate))
                Glide.with(this)
                    .load(us.avatarUrl)
                    .transform(RoundedCorners(100))
                    .into(binding.avatarImage)
            }
        }

        requireActivity().addMenuProvider(MenuProvider(), viewLifecycleOwner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.findViewById<View>(R.id.toolbar)?.visibility = View.VISIBLE
            activity.supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(false);
                it.setHomeButtonEnabled(false);
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class MenuProvider : androidx.core.view.MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.logged_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_exit -> {
                    this@SecondFragment.findNavController().popBackStack()
                    true
                }

                else -> false
            }
        }

    }

    companion object {
        const val TAG = "SecondFragment"
        const val ARG_USER = "user"
    }
}