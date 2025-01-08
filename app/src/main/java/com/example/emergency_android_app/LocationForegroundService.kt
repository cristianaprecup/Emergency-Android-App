package com.example.emergency_android_app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocationForegroundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 2000
    private var isRunning = false

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val BROADCAST_LOCATION_STATUS = "com.example.emergency_android_app.LOCATION_STATUS"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d("LocationService", "Starting service")
                startForegroundService()
            }
            ACTION_STOP -> {
                Log.d("LocationService", "Stopping service")
                stopForegroundService()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        if (isRunning) {
            Log.d("LocationService", "Service already running")
            return
        }
        isRunning = true

        val notification = createNotification("Monitoring location status...")
        startForeground(NOTIFICATION_ID, notification)

        handler.post(object : Runnable {
            override fun run() {
                val isLocationEnabled = checkLocationServices()
                Log.d("LocationService", "Location status: $isLocationEnabled")
                sendLocationStatusBroadcast(isLocationEnabled)
                handler.postDelayed(this, checkInterval)
            }
        })
    }

    private fun stopForegroundService() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        stopSelf()
    }

    private fun checkLocationServices(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }

    private fun sendLocationStatusBroadcast(isLocationEnabled: Boolean) {
        val intent = Intent(BROADCAST_LOCATION_STATUS)
        intent.putExtra("isLocationEnabled", isLocationEnabled)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
