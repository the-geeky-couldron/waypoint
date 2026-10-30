package com.waypoint.app

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class NavigationService : Service() {
    private val controller get() = (application as WaypointApplication).navigation
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("navigation","Active navigation",NotificationManager.IMPORTANCE_LOW))
        startForeground(10,notification("Navigation is starting"))
        scope.launch {
            controller.state.collect { s ->
                if(s.navigating) {
                    val text=if(!s.gpsReady) s.gpsStatus else s.route?.steps?.getOrNull(s.progress?.nextStep ?: 0)?.instruction ?: "Following your route"
                    getSystemService(NotificationManager::class.java).notify(10,notification(text))
                }
            }
        }
    }
    private fun notification(text: String): Notification {
        val open=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop=PendingIntent.getService(this,1,Intent(this,NavigationService::class.java).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this,"navigation").setSmallIcon(R.drawable.ic_navigation)
            .setContentTitle("Waypoint · ${controller.state.value.selected?.name ?: "Navigation"}")
            .setContentText(text).setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION).addAction(0,"End navigation",stop).build()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.action=="STOP") { controller.stopNavigation(); stopSelf() }
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { scope.cancel(); controller.serviceStopped(); super.onDestroy() }
}
