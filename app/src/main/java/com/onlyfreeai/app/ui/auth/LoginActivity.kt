package com.onlyfreeai.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.onlyfreeai.app.MainActivity
import com.onlyfreeai.app.R
import com.onlyfreeai.app.databinding.ActivityLoginBinding
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.show
import com.onlyfreeai.app.util.toast

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"

        /**
         * The Web Client ID from your google-services.json (client_type: 3).
         * This MUST match exactly — it is the OAuth 2.0 Web Client ID from
         * Google Cloud Console → Credentials, NOT the Android client ID.
         *
         * If R.string.default_web_client_id is auto-generated and correct,
         * this constant acts as a fallback / explicit override.
         */
        private const val WEB_CLIENT_ID =
            "533784515869-lrjf3le9ri6ogamstetlr964qqu0uuov.apps.googleusercontent.com"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()

    private var isSignUpMode = false

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            Log.w(TAG, "Google sign-in cancelled (no data returned)")
            setAuthLoading(false)
            toast("Sign in cancelled.")
            return@registerForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                Log.e(TAG, "Google sign-in succeeded but no ID token was returned.")
                setAuthLoading(false)
                toast("Sign in failed: no ID token received.")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed, statusCode=${e.statusCode}, message=${e.message}", e)
            setAuthLoading(false)

            val userMsg = when (e.statusCode) {
                10 -> "Configuration error. Please contact support. (Code 10)"
                7  -> "Network error. Please check your connection."
                12501 -> "Sign in cancelled."
                12502 -> "Sign in is already in progress."
                else -> "Sign in failed (code ${e.statusCode}). Please try again."
            }
            toast(userMsg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip login if already signed in
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setupGoogleSignIn()
        setupClickListeners()
        updateUI()
    }

    private fun setupGoogleSignIn() {
        // Use the hardcoded Web Client ID directly.
        // This avoids the problem where R.string.default_web_client_id is either
        // missing or maps to the wrong value.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        // Google Sign-In
        binding.btnGoogleSignIn.setOnClickListener {
            setAuthLoading(true)
            // Sign out of the previous Google session first so the account picker always shows
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            }
        }

        // Email/Password action (Sign In or Sign Up)
        binding.btnAction.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                toast(getString(R.string.error_empty_email))
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Please enter a valid email address")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                toast(getString(R.string.error_empty_password))
                return@setOnClickListener
            }
            if (password.length < 6) {
                toast(getString(R.string.error_password_short))
                return@setOnClickListener
            }

            if (isSignUpMode) {
                val confirmPassword = binding.etConfirmPassword.text.toString().trim()
                if (password != confirmPassword) {
                    toast(getString(R.string.error_password_mismatch))
                    return@setOnClickListener
                }
                signUpWithEmail(email, password)
            } else {
                signInWithEmail(email, password)
            }
        }

        // Toggle Sign In / Sign Up
        binding.tvSwitchAction.setOnClickListener {
            isSignUpMode = !isSignUpMode
            updateUI()
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                toast(getString(R.string.error_empty_email))
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Please enter a valid email address")
                return@setOnClickListener
            }
            setAuthLoading(true)
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    setAuthLoading(false)
                    if (task.isSuccessful) {
                        toast(getString(R.string.reset_email_sent))
                    } else {
                        val errorMsg = task.exception?.localizedMessage
                            ?: getString(R.string.error_generic)
                        toast(errorMsg)
                    }
                }
        }
    }

    private fun updateUI() {
        if (isSignUpMode) {
            binding.tvTitle.text = getString(R.string.sign_up_title)
            binding.btnAction.text = getString(R.string.sign_up_title)
            binding.layoutConfirmPassword.visibility = View.VISIBLE
            binding.tvForgotPassword.visibility = View.GONE
            binding.tvSwitchLabel.text = getString(R.string.have_account)
            binding.tvSwitchAction.text = getString(R.string.sign_in_title)
        } else {
            binding.tvTitle.text = getString(R.string.sign_in_title)
            binding.btnAction.text = getString(R.string.sign_in_title)
            binding.layoutConfirmPassword.visibility = View.GONE
            binding.tvForgotPassword.visibility = View.VISIBLE
            binding.tvSwitchLabel.text = getString(R.string.no_account)
            binding.tvSwitchAction.text = getString(R.string.sign_up_title)
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        setAuthLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    onAuthSuccess()
                } else {
                    setAuthLoading(false)
                    val errorMsg = task.exception?.localizedMessage
                        ?: getString(R.string.error_sign_in_failed)
                    Log.e(TAG, "Email sign-in failed: $errorMsg", task.exception)
                    toast(errorMsg)
                }
            }
    }

    private fun signUpWithEmail(email: String, password: String) {
        setAuthLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    onAuthSuccess()
                } else {
                    setAuthLoading(false)
                    val errorMsg = task.exception?.localizedMessage
                        ?: getString(R.string.error_sign_up_failed)
                    Log.e(TAG, "Email sign-up failed: $errorMsg", task.exception)
                    toast(errorMsg)
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    onAuthSuccess()
                } else {
                    Log.e(TAG, "signInWithCredential failed", task.exception)
                    if (!isFinishing && !isDestroyed) {
                        setAuthLoading(false)
                        toast("Authentication failed: ${task.exception?.localizedMessage}")
                    }
                }
            }
    }

    private fun onAuthSuccess() {
        lifecycleScope.launch {
            try {
                viewModel.saveUserToFirestore()
            } catch (e: Exception) {
                Log.e(TAG, "Firestore save error", e)
                // Still navigate — user IS authenticated, just warn about sync
                if (!isFinishing && !isDestroyed) {
                    toast("Signed in, but profile sync failed. It will retry.")
                }
            }
            if (!isFinishing && !isDestroyed) {
                setAuthLoading(false)
                navigateToMain()
            }
        }
    }

    /** Disable all interactive elements during auth to prevent double-tap */
    private fun setAuthLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAction.isEnabled = !loading
        binding.btnGoogleSignIn.isEnabled = !loading
        binding.tvSwitchAction.isEnabled = !loading
        binding.tvForgotPassword.isEnabled = !loading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
