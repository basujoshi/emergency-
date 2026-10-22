package com.bashurod.bdremote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

/**
 * Visible foreground-service scaffold. MediaProjection/WebRTC capture lifecycle must
 * be implemented and tested with the selected WebRTC SDK before streaming is functional.
 */
class ScreenShareService : Service() {
    companion object {
        const val ACTION_START = "com.bashurod.bdremote.START"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
        private const val CHANNEL_ID = "bd_remote_screen"
        private const val NOTIFICATION_ID = 7101
    }

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Remote support session", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("BD Remote Support is active")
                .setContentText("Screen sharing requested. Open the app to stop the session.")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("BD Remote Support is active")
                .setContentText("Screen sharing requested.")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true)
                .build()
        }
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // TODO: validate ACTION_START and non-null consent result.
        // TODO: create MediaProjection using the approved result exactly once per session.
        // TODO: initialize WebRTC ScreenCapturerAndroid and send its video track to the peer.
        // TODO: stop projection and release all resources on disconnect/onDestroy.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // TODO: stop WebRTC, release MediaProjection and close signaling socket.
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
