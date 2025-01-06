package com.example.emergency_android_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

object EmergencyUtils {

    private val emergencyNumber = "456872" // test

    fun handleEmergency(context: Context, savedContacts: List<String>) {
        fetchLocation(context) { location ->
            val locationMessage = if (location != null) {
                "Help! My current location is: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "Help! I need assistance."
            }

            savedContacts.forEach { contact ->
                sendSMS(context, contact, locationMessage)
            }

            makeEmergencyCall(context)
        }
    }

    private fun fetchLocation(context: Context, onLocationFetched: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationFetched(null)
            return
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocationFetched(location)
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)
        } catch (e: Exception) {
            e.printStackTrace()
            onLocationFetched(null)
        }
    }

    private fun sendSMS(context: Context, contact: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(contact, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorDialog(context, "Failed to send SMS to $contact")
        }
    }

    private fun makeEmergencyCall(context: Context) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$emergencyNumber")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(callIntent)
            } else {
                showErrorDialog(context, "Permission to make a call is not granted.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorDialog(context, "Failed to make an emergency call.")
        }
    }

    private fun showErrorDialog(context: Context, message: String) {
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
