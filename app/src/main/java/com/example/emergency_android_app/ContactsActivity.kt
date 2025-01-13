package com.example.emergency_android_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContactsActivity : AppCompatActivity() {

    private lateinit var addContactButton: Button
    private lateinit var deleteContactButton: Button
    private lateinit var contactsListView: ListView
    private val contactsList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val agendaRequestCode = 2

    private companion object {
        const val PREFS_NAME = "user_prefs"
        const val KEY_DARK_MODE = "isDarkMode"
    }
    private lateinit var dbHelper: ContactsDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        contactsListView = findViewById(R.id.contactsListView)
        addContactButton = findViewById(R.id.addContactButton)
        deleteContactButton = findViewById(R.id.deleteContactButton)

        dbHelper = ContactsDatabaseHelper(this)

        loadContactsFromDatabase()

        dbHelper = ContactsDatabaseHelper(this)

        loadContactsFromDatabase()

        val isDarkMode = getDarkMode(this)
        applyBackgroundColor(isDarkMode)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, contactsList)
        contactsListView.adapter = adapter
        contactsListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        addContactButton.setOnClickListener {
            val intent = Intent(this, AgendaActivity::class.java)
            startActivityForResult(intent, agendaRequestCode)
        }

        deleteContactButton.setOnClickListener {
            deleteSelectedContacts()
        }
    }

    private fun loadContactsFromDatabase() {
        contactsList.clear()
        contactsList.addAll(dbHelper.getAllContacts())
    }

    private fun deleteSelectedContacts() {
        val selectedPositions = contactsListView.checkedItemPositions
        if (selectedPositions.size() > 0) {
            AlertDialog.Builder(this)
                .setTitle("Delete Contacts")
                .setMessage("Are you sure you want to delete the selected contacts?")
                .setPositiveButton("Yes") { _, _ ->
                    for (i in selectedPositions.size() - 1 downTo 0) {
                        if (selectedPositions.valueAt(i)) {
                            val contactToDelete = contactsList[selectedPositions.keyAt(i)]
                            contactsList.removeAt(selectedPositions.keyAt(i))
                            dbHelper.deleteContact(contactToDelete)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Selected contacts deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            Toast.makeText(this, "No contacts selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == agendaRequestCode && resultCode == Activity.RESULT_OK) {
            val selectedContacts = data?.getStringArrayListExtra("selected_contacts")
            if (!selectedContacts.isNullOrEmpty()) {
                for (contact in selectedContacts) {
                    if (!contactsList.contains(contact)) {
                        contactsList.add(contact)
                        dbHelper.addContact(contact)
                    }
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Contacts added: ${selectedContacts.joinToString(", ")}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No contacts selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false) // Default is light mode
    }


    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    private fun applyBackgroundColor(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.gray_800 else R.color.light_gray
        findViewById<RelativeLayout>(R.id.rootLayout).setBackgroundResource(backgroundColor)
    }
}