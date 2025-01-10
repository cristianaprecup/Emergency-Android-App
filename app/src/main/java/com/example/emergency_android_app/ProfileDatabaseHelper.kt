package com.example.emergency_android_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ProfileDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "profile_db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_PROFILE = "profile"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DOB = "dob"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_ALLERGIES = "allergies"
        private const val COLUMN_DISEASES = "diseases"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PROFILE_TABLE = ("CREATE TABLE $TABLE_PROFILE (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_DOB TEXT, " +
                "$COLUMN_GENDER TEXT, " +
                "$COLUMN_HEIGHT TEXT, " +
                "$COLUMN_ALLERGIES TEXT, " +
                "$COLUMN_DISEASES TEXT" +
                ")")
        db?.execSQL(CREATE_PROFILE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    fun insertProfile(profile: Profile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, profile.name)
            put(COLUMN_DOB, profile.dob)
            put(COLUMN_GENDER, profile.gender)
            put(COLUMN_HEIGHT, profile.height)
            put(COLUMN_ALLERGIES, profile.allergies)
            put(COLUMN_DISEASES, profile.diseases)
        }
        db.insert(TABLE_PROFILE, null, values)
        db.close()
    }

    fun getProfile(): Profile? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILE,
            null, 
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC",
            "1"
        )

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val dob = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB))
            val gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER))
            val height = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT))
            val allergies = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALLERGIES))
            val diseases = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISEASES))

            cursor.close()
            Profile(name, dob, gender, height, allergies, diseases)
        } else {
            cursor.close()
            null
        }
    }

}
