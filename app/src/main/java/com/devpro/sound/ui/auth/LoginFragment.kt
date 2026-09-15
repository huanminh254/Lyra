package com.devpro.sound.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
            val email = binding.loginEmail.text?.toString()?.trim().orEmpty()
            val password = binding.loginPassword.text?.toString().orEmpty()
            if (email.isBlank()) {
                binding.loginEmailLayout.error = "Vui lòng nhập email"
                return@setOnClickListener
            }

            if (password.isBlank()) {
                binding.loginPasswordLayout.error = "Vui lòng nhập mật khẩu"
                return@setOnClickListener
            }

            viewModel.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.loginSubmit.isEnabled = !state.isLoading

            if (state.isSuccess) {
                // MainActivity's FirebaseAuth listener opens Discover after login.
            }

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
}
