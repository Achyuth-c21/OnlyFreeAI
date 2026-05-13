package com.onlyfreeai.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class OnlyFreeAIApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val prefs = getSharedPreferences(com.onlyfreeai.app.util.Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(prefs.getInt(com.onlyfreeai.app.util.Constants.PREF_DARK_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))

        // Enable Firestore offline persistence (modern API)
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Settings already applied or Firestore already accessed
        }

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel = NotificationChannel(
                "onlyfreeai_default",
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from OnlyFreeAI"
            }

            val toolOfDayChannel = NotificationChannel(
                "tool_of_the_day",
                "Tool of the Day",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily featured free AI tool"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(defaultChannel)
            manager.createNotificationChannel(toolOfDayChannel)
        }
    }
}
