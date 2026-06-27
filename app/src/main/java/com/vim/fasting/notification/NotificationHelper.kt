package com.vim.fasting.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.vim.fasting.MainActivity
import com.vim.fasting.R

/**
 * Manages notification channels and sends notifications with vibration.
 * Uses classic NotificationCompat for broad compatibility.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
            setBypassDnd(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Show a notification with vibration and a progress bar for the current timer state.
     *
     * @param title       Notification title
     * @param body        Notification body text
     * @param progressMax Max progress value (e.g. total duration in minutes)
     * @param progress    Current progress value
     * @param ongoing     Whether it's an ongoing (non-dismissible) notification
     */
    fun showProgressNotification(
        title: String,
        body: String,
        progressMax: Int,
        progress: Int,
        ongoing: Boolean = true
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(ongoing)
            .setAutoCancel(false)
            .setContentIntent(tapPendingIntent)
            .setProgress(progressMax, progress, false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show a one-shot notification with vibration (for phase transitions).
     */
    fun showAlertNotification(title: String, body: String) {
        vibrate()

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    /**
     * Cancel the ongoing progress notification.
     * Leaves alert notifications untouched so the user sees the transition notification.
     */
    fun cancelProgressNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    companion object {
        private const val CHANNEL_ID = "circlefast_fasting"
        private const val NOTIFICATION_ID = 100
        private const val NOTIFICATION_ID_ALERT = 101
    }
}
