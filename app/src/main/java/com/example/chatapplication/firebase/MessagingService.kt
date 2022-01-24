package com.example.chatapplication.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatapplication.R
import com.example.chatapplication.activity.ChatActivity
import com.example.chatapplication.model.User
import com.example.chatapplication.utilities.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MessagingService: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val user = User(
            p0.data.get(KEY_NAME)!!,
            "",
            "",
            p0.data.get(KEY_FCM_TOKEN),
            p0.data.get(KEY_USER_ID)
        )
        val notificationId = Random.nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(KEY_USER, user)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(user.name)
        builder.setContentText(p0.data.get(KEY_MESSAGE))
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(
            p0.data.get(KEY_MESSAGE)
        ))
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Chat Message"
            val channelDescription = "This notification channel is used for chat message notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, builder.build())

    }
}