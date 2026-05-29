package com.onlyfreeai.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.onlyfreeai.app.databinding.ActivityMainBinding
import com.onlyfreeai.app.ui.auth.LoginActivity

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
    }

    override fun onResume() {
        super.onResume()
        checkSessionActive()
    }

    /**
     * Verify the Firebase Auth session is still valid on every resume.
     * If the user was disabled/deleted server-side, force logout immediately.
     */
    private fun checkSessionActive() {
        val user = auth.currentUser ?: return
        user.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful && task.exception != null) {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
