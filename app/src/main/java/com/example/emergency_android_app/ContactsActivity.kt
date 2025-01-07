package com.example.emergency_android_app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContactsActivity : AppCompatActivity() {

    private lateinit var addContactButton: Button
    private lateinit var deleteContactButton: Button
    private lateinit var contactsListView: ListView
    private val contactsList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val agendaRequestCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        contactsListView = findViewById(R.id.contactsListView)
        addContactButton = findViewById(R.id.addContactButton)
        deleteContactButton = findViewById(R.id.deleteContactButton)

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

    private fun deleteSelectedContacts() {
        val selectedPositions = contactsListView.checkedItemPositions
        if (selectedPositions.size() > 0) {
            AlertDialog.Builder(this)
                .setTitle("Delete Contacts")
                .setMessage("Are you sure you want to delete the selected contacts?")
                .setPositiveButton("Yes") { _, _ ->
                    for (i in selectedPositions.size() - 1 downTo 0) {
                        if (selectedPositions.valueAt(i)) {
                            contactsList.removeAt(selectedPositions.keyAt(i))
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
                contactsList.addAll(selectedContacts)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Contacts added: ${selectedContacts.joinToString(", ")}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No contacts selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
