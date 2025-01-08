package com.example.emergency_android_app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Switch
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

    // Declare the manage profile button
    private lateinit var manageProfileButton: Button

    private lateinit var themeSwitch: Switch

    // SharedPreferences keys
    private companion object {
        const val PREFS_NAME = "user_prefs"
        const val KEY_DARK_MODE = "isDarkMode"
        const val TAG = "MainActivity" // For logging
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        locationServicesStatus = findViewById(R.id.locationServicesStatus)
        locationServicesLabel = findViewById(R.id.locationServicesLabel)
        helpButton = findViewById(R.id.helpButton)
        manageProfileButton = findViewById(R.id.manageProfile)
        manageContactsButton = findViewById(R.id.manageContactsButton)
        themeSwitch = findViewById(R.id.themeSwitch)

        // Set up location services
        locationServicesLabel.setOnClickListener {
            GoogleMapsUtils.openGoogleMapsWithSearch(this)
        }

        locationHandler = LocationHandler(this) { isLocationEnabled ->
            handleLocationStatusChange(isLocationEnabled)
        }
        locationHandler.start()

        // Set up help button
        helpButton.setOnClickListener {
            if (allPermissionsGranted()) {
                EmergencyUtils.handleEmergency(this, savedContacts)
            } else {
                requestPermissions()
            }
        }

        // Set up manage profile button
        manageProfileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Set up manage contacts button
        manageContactsButton.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        // Set up theme switch
        val isDarkMode = getDarkMode(this)
        themeSwitch.isChecked = isDarkMode
        applyBackgroundColor(isDarkMode) // Apply the initial background color
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleTheme(isChecked)
        }

        // Request permissions if not granted
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        // Implement logic to refresh the contacts list UI
    }

    private fun toggleTheme(isDarkMode: Boolean) {
        Log.d(TAG, "Toggle theme: isDarkMode = $isDarkMode")
        saveDarkMode(this, isDarkMode)
        applyBackgroundColor(isDarkMode) // Apply the new background color
    }

    private fun applyBackgroundColor(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.gray_800 else R.color.light_gray
        findViewById<RelativeLayout>(R.id.rootLayout).setBackgroundResource(backgroundColor)
    }

    private fun saveDarkMode(context: Context, isDarkMode: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    private fun getDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false) // Default is light mode
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHandler.stop()
        dismissLocationRequiredDialog()
    }
}