package com.onlyfreeai.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class OnlyFreeAIApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

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
