package com.example.emergency_android_app

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

object EmergencyUtils {

    private const val EMERGENCY_NR = "456872" // test
    private const val TAG = "EmergencyUtils"

    fun handleEmergency(context: Context, savedContacts: List<String>) {
        Log.d(TAG, "handleEmergency called with contacts: $savedContacts")

        fetchLocation(context) { location ->
            val locationMessage = if (location != null) {
                "Help! My current location is: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "Help! I need assistance."
            }

            val intent = Intent(context, SmsSendingService::class.java)
            intent.putStringArrayListExtra("contacts", ArrayList(savedContacts))
            intent.putExtra("message", locationMessage)
            context.startService(intent)
            Log.d(TAG, "SmsSendingService started with message: $locationMessage")

            makeEmergencyCall(context)
        }
    }

    private fun fetchLocation(context: Context, onLocationFetched: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions not granted")
            onLocationFetched(null)
            return
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d(TAG, "Location received: ${location.latitude}, ${location.longitude}")
                onLocationFetched(location)
                locationManager.removeUpdates(this)
            }

            override fun onProviderEnabled(provider: String) {
                Log.d(TAG, "Location provider enabled: $provider")
            }

            override fun onProviderDisabled(provider: String) {
                Log.d(TAG, "Location provider disabled: $provider")
            }
        }

        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)
            Log.d(TAG, "Requested single location update")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location update", e)
            onLocationFetched(null)
        }
    }

    private fun makeEmergencyCall(context: Context) {
        Log.d(TAG, "makeEmergencyCall called")
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$EMERGENCY_NR")
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                context.startActivity(callIntent)
                Log.d(TAG, "Emergency call started to $EMERGENCY_NR")
            } else {
                Log.w(TAG, "Call permission not granted")
                showErrorDialog(context, "Permission to make a call is not granted.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to make an emergency call", e)
            showErrorDialog(context, "Failed to make an emergency call.")
        }
    }

    private fun showErrorDialog(context: Context, message: String) {
        Log.e(TAG, "Showing error dialog: $message")
        val dialog = AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }
}
