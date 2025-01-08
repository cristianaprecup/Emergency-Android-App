package com.example.emergency_android_app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log

class SmsSendingService : Service() {

    private val tag = "SmsSendingService"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand called")

        val contacts = intent?.getStringArrayListExtra("contacts")
        val message = intent?.getStringExtra("message")

        if (contacts.isNullOrEmpty() || message.isNullOrEmpty()) {
            Log.w(tag, "No contacts or message provided. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(tag, "Contacts to send SMS: $contacts")
        Log.d(tag, "Message to send: $message")

        contacts.forEach { contact ->
            try {
                val smsManager = SmsManager.getDefault()

                smsManager.sendTextMessage(contact, null, message, null, null)
                Log.d(tag, "SMS successfully sent to $contact")
            } catch (e: Exception) {
                Log.e(tag, "Failed to send SMS to $contact", e)
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "SmsSendingService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "SmsSendingService destroyed")
    }
}
