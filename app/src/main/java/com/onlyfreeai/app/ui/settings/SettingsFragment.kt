@file:Suppress("DEPRECATION")
package com.onlyfreeai.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onlyfreeai.app.BuildConfig
import com.onlyfreeai.app.R
import com.onlyfreeai.app.databinding.FragmentSettingsBinding
import com.onlyfreeai.app.ui.auth.LoginActivity
import com.onlyfreeai.app.util.*

@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            requireContext(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                .requestEmail()
                .build()
        )
    }

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
        setupLegal()
        setupLogout()
        setupDeleteAccount()

        // Apply premium tactile feedback
        binding.settingTheme.scalePress()
        binding.settingLanguage.scalePress()
        binding.settingPrivacy.scalePress()
        binding.settingTerms.scalePress()
        binding.settingDelete.scalePress()
        binding.btnLogout.scalePress()

        // Gentle scale-up entrance animation for the avatar photo
        binding.ivAvatar.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(450)
            .withEndAction {
                binding.ivAvatar.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(350)
                    .start()
            }
            .start()

        // Staggered entrance animation for Settings sections
        val viewsToAnimate = listOf(
            binding.cardProfile,
            binding.cardSettingsList,
            binding.btnLogout
        )
        viewsToAnimate.forEachIndexed { index, v ->
            v.animateEntrance(index * 60L)
        }
    }

    private fun setupProfile() {
        val user = auth.currentUser
        binding.tvProfileName.text = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        binding.tvProfileEmail.text = user?.email ?: ""

        // Load profile photo
        val photoUrl = user?.photoUrl?.toString()
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUrl)
                .transform(CircleCrop())
                .into(binding.ivAvatar)
        }
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
                    requireContext().getSharedPreferences(Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE)
                        .edit().putInt(Constants.PREF_DARK_MODE, mode).apply()
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
        } catch (_: Exception) {
            binding.tvVersionValue.text = "1.0.0"
        }
    }

    private fun setupLegal() {
        binding.settingPrivacy.setOnClickListener {
            val intent = Intent(requireContext(), LegalActivity::class.java)
            intent.putExtra(LegalActivity.EXTRA_LEGAL_TYPE, LegalActivity.TYPE_PRIVACY)
            startActivity(intent)
        }
        binding.settingTerms.setOnClickListener {
            val intent = Intent(requireContext(), LegalActivity::class.java)
            intent.putExtra(LegalActivity.EXTRA_LEGAL_TYPE, LegalActivity.TYPE_TERMS)
            startActivity(intent)
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.settings_logout))
                .setMessage(getString(R.string.settings_logout_confirm))
                .setPositiveButton(getString(R.string.settings_logout)) { _, _ ->
                    // Clear session preferences (keep theme)
                    requireContext().getSharedPreferences(
                        Constants.PREFS_NAME,
                        android.content.Context.MODE_PRIVATE
                    ).edit().remove(Constants.PREF_ONBOARDED).apply()

                    // Terminate Firestore persistence, then sign out
                    val db = FirebaseFirestore.getInstance()
                    db.terminate().addOnCompleteListener {
                        db.clearPersistence().addOnCompleteListener {
                            auth.signOut()
                            googleSignInClient.revokeAccess().addOnCompleteListener {
                                val intent = Intent(requireContext(), LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /**
     * SECURITY FIX: Delete Firestore data FIRST, then delete the Firebase Auth account.
     * The original code deleted Auth first, which could leave orphaned Firestore data
     * if the Firestore delete failed (since the auth token was already revoked).
     */
    private fun setupDeleteAccount() {
        binding.settingDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account and all saved data? This action cannot be undone.")
                .setPositiveButton("Delete Permanently") { _, _ ->
                    val user = auth.currentUser
                    val uid = user?.uid
                    if (user != null && uid != null) {
                        requireContext().toast("Deleting account data...")

                        val db = FirebaseFirestore.getInstance()

                        // Step 1: Delete Firestore user document FIRST (while auth token is still valid)
                        db.collection("users").document(uid).delete()
                            .addOnSuccessListener {
                                // Step 2: Now delete the Firebase Auth account
                                user.delete().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        requireContext().toast("Account deleted successfully.")
                                        // Clear local preferences
                                        requireContext().getSharedPreferences(
                                            Constants.PREFS_NAME,
                                            android.content.Context.MODE_PRIVATE
                                        ).edit().clear().apply()

                                        // Navigate to Login
                                        val intent = Intent(requireContext(), LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    } else {
                                        val exception = task.exception
                                        if (exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                            requireContext().toast("Please log out, log in again, and retry deletion for security.")
                                        } else {
                                            requireContext().toast("Failed to delete auth account: ${exception?.localizedMessage}")
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                requireContext().toast("Failed to delete profile data: ${e.localizedMessage}")
                            }
                    } else {
                        requireContext().toast("Not authenticated.")
                    }
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
