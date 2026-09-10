package com.devpro.sound.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devpro.sound.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), SettingsViewModel.Factory())[SettingsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.settingsName.text = user.name
            binding.settingsAccount.text = user.accountSubtitle
            binding.settingsQuality.text = "Audio quality: ${user.audioQuality}"
            binding.settingsWifi.isChecked = user.streamOnlyOnWifi
            binding.settingsDarkMode.isChecked = user.darkModeEnabled
            binding.settingsCache.text = user.cacheSubtitle
            binding.settingsVersion.text = user.appVersion
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.settingsLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.settingsError.visibility = if (message == null) View.GONE else View.VISIBLE
            binding.settingsError.text = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
