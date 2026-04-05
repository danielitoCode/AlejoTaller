package com.elitec.alejotallerscan.infraestructure.core.presentation.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.elitec.alejotallerscan.R
import com.elitec.alejotallerscan.feature.sync.domain.repository.OperatorSyncNotificationService

class OperatorSaleSyncNotificationService(
    private val context: Context
) : OperatorSyncNotificationService {

    override fun showSaleVerified(saleId: String, customerName: String?) {
        createChannelIfRequired()
        if (!canPostNotifications()) return

        val title = "Venta verificada"
        val body = customerName?.takeIf { it.isNotBlank() }?.let {
            "La venta de $it ya fue verificada desde otro operador."
        } ?: "La venta ${saleId.take(8)} ya fue verificada desde otro operador."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(saleId.hashCode(), notification)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createChannelIfRequired() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ventas sincronizadas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifica cuando una venta pendiente ya fue procesada por otro operador."
        }
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "operator_sale_sync"
    }
}
