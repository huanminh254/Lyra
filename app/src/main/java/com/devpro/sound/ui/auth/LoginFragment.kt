package com.devpro.sound.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devpro.sound.R
import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginSubmit.setOnClickListener {
            readCredentials()?.let { (email, password) ->
                viewModel.login(LoginRequest(email = email, password = password))
            }
        }

        binding.loginSignup.setOnClickListener {
            readCredentials()?.let { (email, password) ->
                viewModel.register(LoginRequest(email = email, password = password))
            }
        }

        binding.loginForgotPassword.setOnClickListener {
            val email = binding.loginEmail.text?.toString()?.trim().orEmpty()
            if (email.isBlank()) {
                binding.loginEmailLayout.error = "Nhập email để đặt lại mật khẩu"
            } else {
                binding.loginEmailLayout.error = null
                viewModel.sendPasswordResetEmail(email)
            }
        }

        val socialLoginNotAvailable = View.OnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.social_login_unavailable),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.loginApple.setOnClickListener(socialLoginNotAvailable)
        binding.loginGoogle.setOnClickListener(socialLoginNotAvailable)
        binding.loginSpotify.setOnClickListener(socialLoginNotAvailable)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.loginSubmit.isEnabled = !state.isLoading

            if (state.message.isNotBlank()) {
                Toast.makeText(
                    requireContext(),
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun readCredentials(): Pair<String, String>? {
        val email = binding.loginEmail.text?.toString()?.trim().orEmpty()
        val password = binding.loginPassword.text?.toString().orEmpty()
        binding.loginEmailLayout.error = null
        binding.loginPasswordLayout.error = null

        if (email.isBlank()) {
            binding.loginEmailLayout.error = "Vui lòng nhập email"
            return null
        }

        if (password.isBlank()) {
            binding.loginPasswordLayout.error = "Vui lòng nhập mật khẩu"
            return null
        }

        return email to password
    }
}
