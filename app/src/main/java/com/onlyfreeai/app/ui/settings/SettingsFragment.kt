package com.onlyfreeai.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.onlyfreeai.app.R
import com.onlyfreeai.app.databinding.FragmentSettingsBinding
import com.onlyfreeai.app.ui.auth.LoginActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProfile()
        setupTheme()
        setupLanguage()
        setupVersion()
        setupLogout()
    }

    private fun setupProfile() {
        val user = auth.currentUser
        binding.tvProfileName.text = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        binding.tvProfileEmail.text = user?.email ?: ""
    }

    private fun setupTheme() {
        updateThemeLabel()

        binding.settingTheme.setOnClickListener {
            val themes = arrayOf(
                getString(R.string.settings_theme_light),
                getString(R.string.settings_theme_dark),
                getString(R.string.settings_theme_system)
            )
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val selected = when (currentMode) {
                AppCompatDelegate.MODE_NIGHT_NO -> 0
                AppCompatDelegate.MODE_NIGHT_YES -> 1
                else -> 2
            }

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.settings_theme))
                .setSingleChoiceItems(themes, selected) { dialog, which ->
                    val mode = when (which) {
                        0 -> AppCompatDelegate.MODE_NIGHT_NO
                        1 -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    AppCompatDelegate.setDefaultNightMode(mode)
                    requireContext().getSharedPreferences(com.onlyfreeai.app.util.Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE).edit().putInt(com.onlyfreeai.app.util.Constants.PREF_DARK_MODE, mode).apply()
                    updateThemeLabel()
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun updateThemeLabel() {
        val label = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.settings_theme_light)
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.settings_theme_dark)
            else -> getString(R.string.settings_theme_system)
        }
        binding.tvThemeValue.text = label
    }

    private fun setupLanguage() {
        binding.settingLanguage.setOnClickListener {
            val languages = arrayOf("English")
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.settings_language))
                .setItems(languages) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun setupVersion() {
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.tvVersionValue.text = pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            binding.tvVersionValue.text = "1.0.0"
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.settings_logout))
                .setMessage(getString(R.string.settings_logout_confirm))
                .setPositiveButton(getString(R.string.settings_logout)) { _, _ ->
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
