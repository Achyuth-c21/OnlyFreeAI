package com.onlyfreeai.app.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onlyfreeai.app.R

class OnlyFreeAIMessagingService : FirebaseMessagingService() {

    companion object {
        private val notificationId = java.util.concurrent.atomic.AtomicInteger(0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { notification ->
            val builder = NotificationCompat.Builder(this, "onlyfreeai_default")
                .setSmallIcon(R.drawable.ic_verified_free)
                .setContentTitle(notification.title ?: "OnlyFreeAI")
                .setContentText(notification.body ?: "")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId.incrementAndGet(), builder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to Firestore for targeted push notifications
    }
}
