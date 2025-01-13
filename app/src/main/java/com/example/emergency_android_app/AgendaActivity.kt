package com.example.emergency_android_app

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AgendaActivity : AppCompatActivity() {

    private lateinit var agendaListView: ListView
    private lateinit var confirmSelectionButton: Button
    private val agendaContacts = mutableListOf<String>()

    private companion object {
        const val PREFS_NAME = "user_prefs"
        const val KEY_DARK_MODE = "isDarkMode"
        const val REQUEST_CONTACTS_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        agendaListView = findViewById(R.id.agendaListView)
        confirmSelectionButton = findViewById(R.id.confirmSelectionButton)

        val isDarkMode = getDarkMode(this)
        applyBackgroundColor(isDarkMode)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS_PERMISSION)
        } else {
            loadContacts()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, agendaContacts)
        agendaListView.adapter = adapter
        agendaListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        confirmSelectionButton.setOnClickListener {
            val selectedContacts = mutableListOf<String>()
            val checkedPositions = agendaListView.checkedItemPositions
            for (i in 0 until checkedPositions.size()) {
                val key = checkedPositions.keyAt(i)
                if (checkedPositions.valueAt(i)) {
                    selectedContacts.add(agendaContacts[key])
                }
            }

            val intent = Intent()
            intent.putStringArrayListExtra("selected_contacts", ArrayList(selectedContacts))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun loadContacts() {
        val resolver: ContentResolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                agendaContacts.add("$name: $phoneNumber")
            }
            cursor.close()
        } else {
            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, agendaContacts)
        agendaListView.adapter = adapter
        agendaListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    private fun getDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false) // Default is light mode
    }

    private fun applyBackgroundColor(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.gray_800 else R.color.light_gray
        findViewById<RelativeLayout>(R.id.rootLayout).setBackgroundResource(backgroundColor)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CONTACTS_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            Toast.makeText(this, "Permission to access contacts denied", Toast.LENGTH_SHORT).show()
        }
    }
}