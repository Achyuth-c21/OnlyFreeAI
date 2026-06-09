package com.onlyfreeai.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.onlyfreeai.app.data.model.User
import com.onlyfreeai.app.data.repository.UserRepository
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveUserToFirestore() {
        val firebaseUser = auth.currentUser ?: return

        try {
            val token = try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                ""
            }

            val existingUser = userRepository.getCurrentUser()
            if (existingUser == null) {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isAdmin = false,
                    fcmToken = token
                )
                userRepository.createOrUpdateUser(newUser)
            } else {
                if (token.isNotEmpty() && existingUser.fcmToken != token) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection(User.COLLECTION)
                        .document(firebaseUser.uid)
                        .update("fcmToken", token)
                        .await()
                }
            }
        } catch (e: Exception) {
            com.onlyfreeai.app.util.Logger.e("LoginViewModel", "Failed to save user to Firestore", e)
            throw e // Propagate so caller can show appropriate feedback
        }
    }
}
