package com.example.emergency_android_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactsDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_CONTACTS = "contacts"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CONTACT = "contact"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CONTACTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CONTACT TEXT NOT NULL  
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun addContact(contact: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CONTACT, contact)
        db.insert(TABLE_CONTACTS, null, values)
        db.close()
    }

    fun deleteContact(contact: String) {
        val db = writableDatabase
        db.delete(TABLE_CONTACTS, "$COLUMN_CONTACT=?", arrayOf(contact))
        db.close()
    }

    fun getAllContacts(): List<String> {
        val contacts = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CONTACTS, arrayOf(COLUMN_CONTACT), null, null, null, null, null)
        while (cursor.moveToNext()) {
            contacts.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)))
        }
        cursor.close()
        db.close()
        return contacts
    }
}
