package com.example.emergency_android_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var locationServicesStatus: TextView
    private lateinit var locationServicesLabel: TextView
    private lateinit var locationHandler: LocationHandler
    private var locationDialog: AlertDialog? = null
    private lateinit var helpButton: FrameLayout
    private val savedContacts = listOf("1234567890", "0987654321") // hardcoded for now

    private val requiredPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CALL_PHONE
    )
    private val permissionsRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationServicesStatus = findViewById(R.id.locationServicesStatus)
        locationServicesLabel = findViewById(R.id.locationServicesLabel)

        locationServicesLabel.setOnClickListener {
            GoogleMapsUtils.openGoogleMapsWithSearch(this)
        }

        locationHandler = LocationHandler(this) { isLocationEnabled ->
            handleLocationStatusChange(isLocationEnabled)
        }

        locationHandler.start()

        helpButton = findViewById(R.id.helpButton)

        helpButton.setOnClickListener {
            if (allPermissionsGranted()) {
                EmergencyUtils.handleEmergency(this, savedContacts)
            } else {
                requestPermissions()
            }
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    private fun handleLocationStatusChange(isLocationEnabled: Boolean) {
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
            locationServicesStatus.text = "On"
            locationServicesStatus.setTextColor(getColor(R.color.green))
        } else {
            locationServicesStatus.text = "Off"
            locationServicesStatus.setTextColor(getColor(R.color.red))
        }
    }

    private fun showLocationRequiredDialog() {
        if (locationDialog?.isShowing == true) return

        locationDialog = AlertDialog.Builder(this)
            .setTitle("Location Required")
            .setMessage("This app requires location services to be enabled. Please turn on location services.")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Exit App") { _, _ -> finish() }
            .create()
        locationDialog?.show()
    }

    private fun dismissLocationRequiredDialog() {
        locationDialog?.dismiss()
        locationDialog = null
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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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

    override fun onDestroy() {
        super.onDestroy()
        locationHandler.stop()
        dismissLocationRequiredDialog()
    }
}
