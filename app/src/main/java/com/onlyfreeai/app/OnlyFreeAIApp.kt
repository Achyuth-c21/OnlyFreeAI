package com.onlyfreeai.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class OnlyFreeAIApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Enable StrictMode in debug builds to catch security issues early
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        FirebaseApp.initializeApp(this)

        // Initialize Firebase App Check with Play Integrity for production security
        // This protects backend resources from abuse by verifying the app is genuine
        try {
            val firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        } catch (e: Exception) {
            com.onlyfreeai.app.util.Logger.e("OnlyFreeAIApp", "App Check initialization failed", e)
        }

        // Apply theme preference
        val prefs = getSharedPreferences(
            com.onlyfreeai.app.util.Constants.PREFS_NAME,
            MODE_PRIVATE
        )
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            prefs.getInt(
                com.onlyfreeai.app.util.Constants.PREF_DARK_MODE,
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        )

        // Enable Firestore offline persistence with secure settings
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (_: Exception) {
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
