package com.example.tabata_timer.data.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.tabata_timer.MainActivity
import com.example.tabata_timer.R
import com.example.tabata_timer.domain.model.TimerState
import com.example.tabata_timer.domain.timer.TimerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerManager: TimerManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isServiceRunning = false

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_SKIP = "ACTION_SKIP"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                timerManager.stop()
                stopSelf()
            }
            ACTION_PAUSE -> timerManager.pause()
            ACTION_RESUME -> timerManager.resume()
            ACTION_SKIP -> timerManager.skip()
            else -> {
                if (!isServiceRunning) {
                    startForegroundService()
                    isServiceRunning = true
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification(TimerState.Idle())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            timerManager.timerState.collect { state ->
                if (state is TimerState.Finished || state is TimerState.Idle) {
                    stopSelf()
                } else {
                    updateNotification(state)
                }
            }
        }
    }

    private fun updateNotification(state: TimerState) {
        val notification = createNotification(state)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(state: TimerState): Notification {
        val title = when (state) {
            is TimerState.Preparing -> "Preparing"
            is TimerState.Working -> "Working"
            is TimerState.Resting -> "Resting"
            is TimerState.Paused -> "Paused"
            else -> "Timer"
        }

        val contentText = "Time: ${state.remainingSeconds}s | Round: ${state.currentRound} | Set: ${state.currentSet}"

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (state is TimerState.Paused) {
            NotificationCompat.Action(0, "Resume", getServicePendingIntent(ACTION_RESUME))
        } else {
            NotificationCompat.Action(0, "Pause", getServicePendingIntent(ACTION_PAUSE))
        }

        val stopAction = NotificationCompat.Action(0, "Stop", getServicePendingIntent(ACTION_STOP))
        val skipAction = NotificationCompat.Action(0, "Skip", getServicePendingIntent(ACTION_SKIP))

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(pauseResumeAction)
            .addAction(skipAction)
            .addAction(stopAction)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun getServicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
