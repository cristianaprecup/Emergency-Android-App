package com.example.emergency_android_app

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
    private val predefinedAllergies = listOf("Peanuts", "Shellfish", "Dairy", "Gluten", "Eggs", "Soy", "Tree Nuts", "Wheat", "Fish", "Others")
    private val predefinedDiseases = listOf("Diabetes", "Hypertension", "Asthma", "Cancer", "Heart Disease", "Epilepsy", "Thyroid", "Arthritis", "Anemia", "Depression")

    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 1002
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize UI elements
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

        // Profile image click listener
        profileImage.setOnClickListener {
            showImagePickerDialog()
        }

        // Select Allergies button click listener
        selectAllergiesButton.setOnClickListener {
            showSelectionDialog("Select Allergies", predefinedAllergies, selectedAllergies) { updateSelectedAllergies() }
        }

        // Select Diseases button click listener
        selectDiseasesButton.setOnClickListener {
            showSelectionDialog("Select Diseases", predefinedDiseases, selectedDiseases) { updateSelectedDiseases() }
        }

        // Save profile button click listener
        saveProfileButton.setOnClickListener {
            saveProfile()
        }

        // Check for permissions
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

        // Validate input fields
        if (name.isEmpty() || dob.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Profile Saved")
        builder.setMessage(
            """
            Name: $name
            DOB: $dob
            Gender: $gender
            Height: $height cm
            Allergies: ${if (selectedAllergies.isEmpty()) "None" else selectedAllergies.joinToString(", ")}
            Diseases: ${if (selectedDiseases.isEmpty()) "None" else selectedDiseases.joinToString(", ")}
            """.trimIndent()
        )
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
        builder.show()
    }

    private fun takeSelfie() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            try {
                val photoFile: File = createImageFile()
                imageUri = FileProvider.getUriForFile(this, "com.example.emergency_android_app.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun deleteImage() {
        profileImage.setImageResource(R.drawable.ic_profile_placeholder)
    }

    private fun createImageFile(): File {
        val storageDir = filesDir
        return File.createTempFile("profile_image", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val circularBitmap = getCircularBitmap(bitmap)
                    profileImage.setImageBitmap(circularBitmap)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        val inputStream = contentResolver.openInputStream(selectedImageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val circularBitmap = getCircularBitmap(bitmap)
                        profileImage.setImageBitmap(circularBitmap)
                    }
                }
            }
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        Canvas(output).apply {
            drawCircle(size / 2f, size / 2f, size / 2f, paint)
        }

        return output
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Permissions are required to access the camera and storage", Toast.LENGTH_SHORT).show()
        }
    }
}
