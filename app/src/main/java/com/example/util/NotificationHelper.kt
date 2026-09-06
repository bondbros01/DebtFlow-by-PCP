package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
  private const val CHANNEL_ID = "debtflow_notifications"
  private const val CHANNEL_NAME = "DebtFlow Alerts"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = "Notifications for debt reminders and project updates"
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun sendNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
    if (!AppPersistenceManager.loadEnableNotifications(context, true)) return
    createNotificationChannel(context)
    try {
      val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

      with(NotificationManagerCompat.from(context)) {
        // Requires POST_NOTIFICATIONS permission on Android 13+
        notify(notificationId, builder.build())
      }
    } catch (e: Exception) {
      // Ignored if permissions are missing or system disallows
    }
  }

  fun checkAndTriggerDueDebtNotifications(context: Context, reminders: List<com.example.model.ReminderItem>) {
    val todayFormatted = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date())
    
    reminders.forEach { debt ->
      if (!debt.isPaid) {
        val debtDate = com.example.ui.concreteDateText(debt.relativeDateText)
        val isDueToday = debtDate.equals(todayFormatted, ignoreCase = true) ||
            debt.relativeDateText.contains("Today", ignoreCase = true)
        if (isDueToday) {
          val person = debt.getAssociatedPerson()
          val direction = if (debt.isPositive) "$person owes you" else "You owe $person"
          sendNotification(
            context = context,
            title = "Outstanding Debt Due Today: ${debt.title}",
            message = "$direction $${String.format(java.util.Locale.US, "%.2f", debt.amount)}. Scheduled due date: $debtDate.",
            notificationId = debt.id.hashCode()
          )
        }
      }
    }
  }
}
