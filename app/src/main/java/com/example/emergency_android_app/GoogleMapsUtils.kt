package com.example.emergency_android_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog

object GoogleMapsUtils {

    fun openGoogleMapsWithSearch(context: Context) {
        val query = "hospitals and police stations near me open now"
        val uri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            showGoogleMapsNotInstalledError(context)
        }
    }

    private fun showGoogleMapsNotInstalledError(context: Context) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Google Maps Not Installed")
            .setMessage("Google Maps is required to perform this action. Please install Google Maps and try again.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }
}
