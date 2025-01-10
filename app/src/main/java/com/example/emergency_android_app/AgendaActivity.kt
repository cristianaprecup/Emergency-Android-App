package com.example.emergency_android_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class AgendaActivity : AppCompatActivity() {

    private lateinit var agendaListView: ListView
    private lateinit var confirmSelectionButton: Button
    private val agendaContacts = listOf(
        "Ela: 123-456-7890",
        "Huda: 234-567-8901",
        "Cristiana: 345-678-9013",
        "Andrei: 163-456-3390",
        "Robert: 258-567-8900",
        "Diana: 315-698-2092",
        "Luisa: 345-222-9012",
        "Alex: 197-456-2290",
        "Ionut: 233-567-8000"
    )

    // SharedPreferences keys
    private companion object {
        const val PREFS_NAME = "user_prefs"
        const val KEY_DARK_MODE = "isDarkMode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        agendaListView = findViewById(R.id.agendaListView)
        confirmSelectionButton = findViewById(R.id.confirmSelectionButton)

        val isDarkMode = getDarkMode(this)
        applyBackgroundColor(isDarkMode)

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

    private fun getDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false) // Default is light mode
    }

    private fun applyBackgroundColor(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.gray_800 else R.color.light_gray
        findViewById<RelativeLayout>(R.id.rootLayout).setBackgroundResource(backgroundColor)
    }
}