package com.example.emergency_android_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class AgendaActivity : AppCompatActivity() {

    private lateinit var agendaListView: ListView
    private lateinit var confirmSelectionButton: Button
    private val agendaContacts = listOf(
        "Ela: 123-456-7890",
        "Huda: 234-567-8901",
        "Cristiana: 345-678-9012",
        "Andrei: 163-456-3390",
        "Robert: 258-567-8900",
        "Diana: 329-698-9092"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        agendaListView = findViewById(R.id.agendaListView)
        confirmSelectionButton = findViewById(R.id.confirmSelectionButton)

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
}
