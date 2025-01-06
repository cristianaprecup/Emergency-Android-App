package com.example.emergency_android_app

import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper

class LocationHandler(
    private val context: Context,
    private val onLocationStatusChanged: (Boolean) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 2000 // check every 2 seconds
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        handler.post(object : Runnable {
            override fun run() {
                val isLocationEnabled = checkLocationServices()
                onLocationStatusChanged(isLocationEnabled)
                handler.postDelayed(this, checkInterval)
            }
        })
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkLocationServices(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
