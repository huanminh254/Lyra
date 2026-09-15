package com.devpro.sound.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.devpro.sound.R
import com.devpro.sound.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()
    }

    override fun onResume() {
        super.onResume()
        setLightStatusBar()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.screen_background)
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = false
    }

    private fun setLightStatusBar() {
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.login_background)
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
