package com.example.emergency_android_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddContactActivity : AppCompatActivity() {
    private lateinit var contactNameEditText: EditText
    private lateinit var contactPhoneEditText: EditText
    private lateinit var saveContactButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_contact)

        contactNameEditText = findViewById(R.id.contactName)
        contactPhoneEditText = findViewById(R.id.contactPhone)
        saveContactButton = findViewById(R.id.saveContactButton)

        saveContactButton.setOnClickListener {
            val contactName = contactNameEditText.text.toString()
            val contactPhone = contactPhoneEditText.text.toString()

            val resultIntent = Intent()
            resultIntent.putExtra("contact_name", contactName)
            resultIntent.putExtra("contact_phone", contactPhone)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
