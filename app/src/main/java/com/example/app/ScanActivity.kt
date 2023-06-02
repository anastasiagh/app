package com.example.app

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.speech.SpeechRecognizer
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.app.databinding.ActivityMainBinding
import com.example.app.databinding.ActivityScanBinding
import com.google.android.gms.vision.text.TextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
//import com.squareup.okhttp.OkHttpClient
//import com.squareup.okhttp.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.security.Permissions
import java.util.jar.Manifest
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder


class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding

    private companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri: Uri? = null

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private lateinit var progressDialog: ProgressDialog

    private lateinit var textRecognizer: com.google.mlkit.vision.text.TextRecognizer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.takeImageButton.setOnClickListener {
            showImage()
        }

        binding.recognizeTextButton.setOnClickListener {
            if (imageUri == null) {
                showToast("pick image")
            } else {
                recognizeTextFromImage()
            }
        }
    }

    private fun recognizeTextFromImage() {
        progressDialog.setMessage("Preparing Image")
        progressDialog.show()

        try {
            val inputImage = InputImage.fromFilePath(this, imageUri!!)

            progressDialog.setMessage("Recognizing text")

            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener { text ->
                progressDialog.dismiss()

                val recognizedText = text.text
                print(recognizedText)
//                try {
//                    val client = OkHttpClient()
//                    val url = URL("https://reqres.in/api/users?page=2")
//
//                    val request = Request.Builder()
//                            .url(url)
//                            .get()
//                            .build()
//
//                    val response = client.newCall(request).execute()
//
//                    val responseBody = response.body!!.string()
//
//                    //Response
//                    println("Response Body: " + responseBody)
//
//                    //we could use jackson if we got a JSON
//                    val mapperAll = ObjectMapper()
//                    val objData = mapperAll.readTree(responseBody)
//
//                    objData.get("data").forEachIndexed { index, jsonNode ->
//                        println("$index $jsonNode")
//                    }
//
//                } catch (e: Exception) {
//                        showToast("Failed ${e.message}")
//                }
                    binding.recognizedTextInput.setText(recognizedText)
            }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    showToast("Failed ${e.message}")
                }


        } catch (e: Exception) {
            progressDialog.dismiss()
            showToast("Failed ${e.message}")
        }
    }

    private fun showImage() {
        val popUpMenu = PopupMenu(this, binding.image)

        popUpMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popUpMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popUpMenu.show()

        popUpMenu.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId

            if (id == 1){
                if (checkCameraPermission()){
                    pickImageCamera()
                } else {
                    requestCameraPermission()
                }
            } else if(id == 2) {
                if (checkStoragePermission()){
                    pickImageGallery()
                } else {
                    requestStoragePermission()
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResult.launch(intent)
    }

    private val galleryActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            imageUri = data!!.data

            binding.image.setImageURI(imageUri)

        } else {
            showToast("Cancelled")
        }
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.image.setImageURI(imageUri)

            } else {
                showToast("Cancelled")
            }
        }

    private fun checkStoragePermission() : Boolean {
        return  ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission() : Boolean {
        val cameraResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted){
                        pickImageCamera()
                    } else {
                        showToast("Camera and Storage permissions are required")
                    }
                }
            }

            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storageAccepted){
                        pickImageGallery()
                    } else {
                        showToast("Storage permissions are required")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}