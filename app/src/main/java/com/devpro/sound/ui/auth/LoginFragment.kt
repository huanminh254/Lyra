package com.devpro.sound.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devpro.sound.R
import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private var isRegisterMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginSubmit.setOnClickListener { submitCredentials() }

        binding.loginSignup.setOnClickListener {
            setRegisterMode(!isRegisterMode)
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

        setRegisterMode(false)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.loginSubmit.isEnabled = !state.isLoading
            binding.loginSignup.isEnabled = !state.isLoading

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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginEmailLayout.error = getString(R.string.invalid_email)
            return null
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.loginPasswordLayout.error = getString(R.string.password_too_short)
            return null
        }

        return email to password
    }

    private fun submitCredentials() {
        readCredentials()?.let { (email, password) ->
            if (isRegisterMode) {
                val name = binding.loginName.text?.toString()?.trim().orEmpty()
                binding.loginNameLayout.error = null
                if (name.isBlank()) {
                    binding.loginNameLayout.error = getString(R.string.name_required)
                    return
                }

                val confirmPassword = binding.loginConfirmPassword.text?.toString().orEmpty()
                if (password != confirmPassword) {
                    binding.loginConfirmPasswordLayout.error = getString(R.string.password_mismatch)
                    return
                }

                viewModel.register(
                    LoginRequest(
                        email = email,
                        password = password,
                        name = name
                    )
                )
            } else {
                viewModel.login(LoginRequest(email = email, password = password))
            }
        }
    }

    private fun setRegisterMode(registerMode: Boolean) {
        isRegisterMode = registerMode
        binding.loginTitle.setText(
            if (registerMode) R.string.create_account else R.string.welcome_back
        )
        binding.loginSubtitle.setText(
            if (registerMode) R.string.register_subtitle else R.string.login_subtitle
        )
        binding.loginSubmit.setText(
            if (registerMode) R.string.signup else R.string.login
        )
        binding.loginSignup.setText(
            if (registerMode) R.string.login_prompt else R.string.signup_prompt
        )
        binding.loginForgotPassword.isVisible = !registerMode
        binding.loginNameLayout.isVisible = registerMode
        binding.loginConfirmPasswordLayout.isVisible = registerMode
        (binding.loginEmailLayout.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            params.topMargin = dp(if (registerMode) 12 else 48)
            binding.loginEmailLayout.layoutParams = params
        }
        binding.loginEmailLayout.error = null
        binding.loginPasswordLayout.error = null
        binding.loginNameLayout.error = null
        binding.loginConfirmPasswordLayout.error = null
        if (!registerMode) {
            binding.loginName.text?.clear()
            binding.loginConfirmPassword.text?.clear()
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }
}
