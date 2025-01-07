package com.example.emergency_android_app

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private lateinit var locationServicesStatus: TextView
    private lateinit var locationServicesLabel: TextView
    private lateinit var locationStatusReceiver: BroadcastReceiver
    private var locationDialog: AlertDialog? = null
    private lateinit var helpButton: FrameLayout
    private val savedContacts = listOf("1234567890", "0987654321") // hardcoded for now
    private lateinit var addContactButton: Button
    private val contactsList = mutableListOf("1234567890", "0987654321")
    private val addContactRequestCode = 1
    private lateinit var manageContactsButton: Button

    private val requiredPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CALL_PHONE
    )
    private val permissionsRequestCode = 1

    private lateinit var manageProfileButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationServicesStatus = findViewById(R.id.locationServicesStatus)
        locationServicesLabel = findViewById(R.id.locationServicesLabel)

        locationServicesLabel.setOnClickListener {
            GoogleMapsUtils.openGoogleMapsWithSearch(this)
        }

        NotificationUtils.createNotificationChannel(this)

        helpButton = findViewById(R.id.helpButton)

        helpButton.setOnClickListener {
            if (allPermissionsGranted()) {
                NotificationUtils.showEmergencyNotification(this)
                EmergencyUtils.handleEmergency(this, savedContacts)
            } else {
                requestPermissions()
            }
        }

        manageProfileButton = findViewById(R.id.manageProfile)

        manageProfileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        manageContactsButton = findViewById(R.id.manageContactsButton)
        manageContactsButton.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        locationStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isLocationEnabled = intent?.getBooleanExtra("isLocationEnabled", false) ?: false
                Log.d("MainActivity", "Received location status broadcast: $isLocationEnabled")
                handleLocationStatusChange(isLocationEnabled)
            }
        }

        val filter = IntentFilter(LocationForegroundService.BROADCAST_LOCATION_STATUS)
        LocalBroadcastManager.getInstance(this).registerReceiver(locationStatusReceiver, filter)
        Log.d("MainActivity", "Receiver registered successfully with LocalBroadcastManager")

        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        serviceIntent.action = LocationForegroundService.ACTION_START
        startService(serviceIntent)
        Log.d("MainActivity", "LocationForegroundService started")
    }

    private fun allPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, permissionsRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionsRequestCode) {
            val allPermissionsGranted = grantResults.isNotEmpty() && grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }

            if (!allPermissionsGranted) {
                AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage("This app requires SMS, Location, and Call permissions to function correctly.")
                    .setPositiveButton("Grant Permissions") { _, _ ->
                        requestPermissions()
                    }
                    .setNegativeButton("Exit") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == addContactRequestCode && resultCode == Activity.RESULT_OK) {
            val contactName = data?.getStringExtra("contact_name")
            val contactPhone = data?.getStringExtra("contact_phone")
            if (contactName != null && contactPhone != null) {
                contactsList.add(contactPhone)
                refreshContactsList()
            }
        }
    }

    private fun refreshContactsList() {
    }

    private fun handleLocationStatusChange(isLocationEnabled: Boolean) {
        Log.d("MainActivity", "Handling location status change: $isLocationEnabled")
        if (isLocationEnabled) {
            updateLocationStatus(true)
            dismissLocationRequiredDialog()
        } else {
            updateLocationStatus(false)
            showLocationRequiredDialog()
        }
    }

    private fun updateLocationStatus(isLocationEnabled: Boolean) {
        if (isLocationEnabled) {
            Log.d("MainActivity", "Location is ON")
            locationServicesStatus.text = "On"
            locationServicesStatus.setTextColor(getColor(R.color.green))
        } else {
            Log.d("MainActivity", "Location is OFF")
            locationServicesStatus.text = "Off"
            locationServicesStatus.setTextColor(getColor(R.color.red))
        }
    }

    private fun showLocationRequiredDialog() {
        if (locationDialog?.isShowing == true) return

        Log.d("MainActivity", "Showing location required dialog")
        locationDialog = AlertDialog.Builder(this)
            .setTitle("Location Required")
            .setMessage("This app requires location services to be enabled. Please turn on location services.")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Exit App") { _, _ -> finish() }
            .create()
        locationDialog?.show()
    }

    private fun dismissLocationRequiredDialog() {
        Log.d("MainActivity", "Dismissing location required dialog")
        locationDialog?.dismiss()
        locationDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLocationRequiredDialog()
        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        serviceIntent.action = LocationForegroundService.ACTION_STOP
        Log.d("MainActivity", "Stopping foreground service")
        startService(serviceIntent)

        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationStatusReceiver)
        Log.d("MainActivity", "Receiver unregistered")
    }
}
