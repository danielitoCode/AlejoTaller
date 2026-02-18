package com.elitec.alejotaller.infraestructure.core.presentation.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.elitec.alejotaller.R
import kotlin.random.Random

class OrderNotificationService(
    private val context: Context
) {

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showOrderStatus(title: String, message: String) {
        createChannelIfRequired()

        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.alejoicon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(Random.nextInt(), notification)
    }

    private fun createChannelIfRequired() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Estado de pedidos",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "order_status_channel"
    }
}