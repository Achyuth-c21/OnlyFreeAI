package com.onlyfreeai.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.onlyfreeai.app.databinding.ActivityMainBinding
import com.onlyfreeai.app.ui.admin.AdminActivity
import com.onlyfreeai.app.ui.auth.LoginActivity
import com.onlyfreeai.app.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        checkSessionActive()
    }

    private fun checkSessionActive() {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val exception = task.exception
                    // If the user was deleted, disabled, or revoked on the server, force logout
                    if (exception != null) {
                        Log.e("MainActivity", "User session is invalid. Force logging out.", exception)
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController ?: return

        binding.bottomNavigation.setupWithNavController(navController)

        // Smoothly hide/show bottom navigation with transitions
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val shouldShow = when (destination.id) {
                R.id.homeFragment,
                R.id.submitToolFragment,
                R.id.settingsFragment -> true
                else -> false
            }

            if (shouldShow) {
                if (binding.bottomNavigation.visibility != View.VISIBLE) {
                    binding.bottomNavigation.slideUp(250)
                    binding.bottomNavShadow.fadeIn(250)
                }
            } else {
                if (binding.bottomNavigation.visibility == View.VISIBLE) {
                    binding.bottomNavigation.slideDown(200)
                    binding.bottomNavShadow.fadeOut(200)
                }
            }
        }
    }
}
