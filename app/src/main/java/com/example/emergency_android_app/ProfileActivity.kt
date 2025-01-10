package com.example.emergency_android_app

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class ProfileActivity : AppCompatActivity() {
    private lateinit var dbHelper: ProfileDatabaseHelper
    private lateinit var profileImage: ImageView
    private lateinit var nameInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var heightInput: EditText
    private lateinit var allergiesTextView: TextView
    private lateinit var diseasesTextView: TextView
    private lateinit var selectAllergiesButton: Button
    private lateinit var selectDiseasesButton: Button
    private lateinit var saveProfileButton: Button

    private val selectedAllergies = mutableListOf<String>()
    private val selectedDiseases = mutableListOf<String>()
    private val predefinedAllergies = listOf(
        "Peanuts", "Shellfish", "Dairy", "Gluten", "Eggs", "Soy", "Tree Nuts", "Wheat", "Fish", "Others"
    )
    private val predefinedDiseases = listOf(
        "Diabetes", "Hypertension", "Asthma", "Cancer", "Heart Disease", "Epilepsy", "Thyroid",
        "Arthritis", "Anemia", "Depression"
    )

    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 1002
    private lateinit var imageUri: Uri

    private companion object {
        const val PREFS_NAME = "user_prefs"
        const val KEY_DARK_MODE = "isDarkMode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = ProfileDatabaseHelper(this)

        profileImage = findViewById(R.id.profileImage)
        nameInput = findViewById(R.id.nameInput)
        dobInput = findViewById(R.id.dobInput)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        heightInput = findViewById(R.id.heightInput)
        allergiesTextView = findViewById(R.id.allergiesInput)
        diseasesTextView = findViewById(R.id.diseasesInput)
        selectAllergiesButton = findViewById(R.id.addAllergyButton)
        selectDiseasesButton = findViewById(R.id.addDiseaseButton)
        saveProfileButton = findViewById(R.id.saveProfileButton)

        val isDarkMode = getDarkMode(this)
        applyBackgroundColor(isDarkMode)

        profileImage.setOnClickListener {
            showImagePickerDialog()
        }

        selectAllergiesButton.setOnClickListener {
            showSelectionDialog("Select Allergies", predefinedAllergies, selectedAllergies) { updateSelectedAllergies() }
        }

        selectDiseasesButton.setOnClickListener {
            showSelectionDialog("Select Diseases", predefinedDiseases, selectedDiseases) { updateSelectedDiseases() }
        }

        saveProfileButton.setOnClickListener {
            saveProfile()
        }

        // Load saved profile data
        val savedProfile = dbHelper.getProfile()
        if (savedProfile != null) {
            nameInput.setText(savedProfile.name)
            dobInput.setText(savedProfile.dob)
            heightInput.setText(savedProfile.height)

            // Set gender
            for (i in 0 until genderRadioGroup.childCount) {
                val radioButton = genderRadioGroup.getChildAt(i) as RadioButton
                if (radioButton.text.toString() == savedProfile.gender) {
                    radioButton.isChecked = true
                    break
                }
            }

            // Set allergies and diseases
            selectedAllergies.addAll(savedProfile.allergies.split(", "))
            updateSelectedAllergies()

            selectedDiseases.addAll(savedProfile.diseases.split(", "))
            updateSelectedDiseases()
        }

        checkPermissions()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Delete Image")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Profile Image Options")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> chooseFromGallery()
                1 -> deleteImage()
            }
        }
        builder.show()
    }

    private fun showSelectionDialog(
        title: String,
        items: List<String>,
        selectedItems: MutableList<String>,
        onUpdate: () -> Unit
    ) {
        val selected = BooleanArray(items.size) { selectedItems.contains(items[it]) }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMultiChoiceItems(items.toTypedArray(), selected) { _, index, isChecked ->
            if (isChecked) {
                selectedItems.add(items[index])
            } else {
                selectedItems.remove(items[index])
            }
        }
        builder.setPositiveButton("OK") { _, _ -> onUpdate() }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun updateSelectedAllergies() {
        allergiesTextView.text = if (selectedAllergies.isEmpty()) "None" else selectedAllergies.joinToString(", ")
    }

    private fun updateSelectedDiseases() {
        diseasesTextView.text = if (selectedDiseases.isEmpty()) "None" else selectedDiseases.joinToString(", ")
    }

    private fun saveProfile() {
        val name = nameInput.text.toString().trim()
        val dob = dobInput.text.toString().trim()
        val height = heightInput.text.toString().trim()
        val genderId = genderRadioGroup.checkedRadioButtonId
        val gender = if (genderId != -1) findViewById<RadioButton>(genderId).text.toString() else "Not specified"

        if (name.isEmpty() || dob.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = Profile(
            name = name,
            dob = dob,
            gender = gender,
            height = height,
            allergies = selectedAllergies.joinToString(", "),
            diseases = selectedDiseases.joinToString(", ")
        )

        dbHelper.insertProfile(profile)
        Toast.makeText(this, "Profile saved to database!", Toast.LENGTH_SHORT).show()
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun deleteImage() {
        profileImage.setImageResource(R.drawable.ic_profile_placeholder)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1001
            )
        }
    }

    private fun getDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    private fun applyBackgroundColor(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) R.color.gray_800 else R.color.light_gray
        findViewById<ScrollView>(R.id.rootLayout).setBackgroundResource(backgroundColor)
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
