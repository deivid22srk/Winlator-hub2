package com.winlator.downloader

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.winlator.downloader.data.AppDownloadManager
import com.winlator.downloader.data.DownloadStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class DownloadService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isForeground = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        observeDownloads()
        return START_STICKY
    }

    private fun observeDownloads() {
        serviceScope.launch {
            AppDownloadManager.tasks.forEach { task ->
                launch {
                    task.progress.collectLatest {
                        updateNotification()
                    }
                }
                launch {
                    task.status.collectLatest {
                        updateNotification()
                        if (AppDownloadManager.tasks.all { it.status.value == DownloadStatus.COMPLETED || it.status.value == DownloadStatus.PAUSED || it.status.value == DownloadStatus.FAILED || it.status.value == DownloadStatus.IDLE }) {
                            stopForeground(STOP_FOREGROUND_DETACH)
                            isForeground = false
                        }
                    }
                }
            }
        }
    }

    private fun updateNotification() {
        val activeTasks = AppDownloadManager.tasks.filter { it.status.value == DownloadStatus.DOWNLOADING }
        if (activeTasks.isEmpty() && isForeground) return

        val notification = createNotification(activeTasks)
        if (!isForeground) {
            startForeground(1, notification)
            isForeground = true
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification)
        }
    }

    private fun createNotification(activeTasks: List<com.winlator.downloader.data.DownloadTask>): Notification {
        val title = if (activeTasks.size == 1) "Baixando ${activeTasks[0].title}" else "Baixando ${activeTasks.size} arquivos"
        val progress = if (activeTasks.isNotEmpty()) activeTasks.sumOf { it.progress.value.toDouble() } / activeTasks.size else 0.0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("${(progress * 100).toInt()}% concluído")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "download_channel"
    }
}
