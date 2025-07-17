package com.example.guavisionapps

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var previewImageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var uploadButton: Button

    private var selectedBitmap: Bitmap? = null
    private lateinit var classifierHelper: ClassifierHelper
    private var cameraImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewImageView = findViewById(R.id.previewImageView)
        resultTextView = findViewById(R.id.resultTextView)
        progressIndicator = findViewById(R.id.progressIndicator)
        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)
        uploadButton = findViewById(R.id.uploadButton)

        classifierHelper = ClassifierHelper(this)

        galleryButton.setOnClickListener { openGallery() }
        cameraButton.setOnClickListener { openCamera() }
        uploadButton.setOnClickListener { classifyImage() }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { launchCrop(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && cameraImageUri != null) {
            launchCrop(cameraImageUri!!)
        }
    }

    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                previewImageView.setImageBitmap(selectedBitmap)
                resultTextView.text = "Gambar siap diklasifikasikan"
            }
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("camera_image", ".jpg", cacheDir)
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            photoFile
        )
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    private fun launchCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
        cropLauncher.launch(uCrop.getIntent(this))
    }

    private fun classifyImage() {
        val bitmap = selectedBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        classifierHelper.classify(bitmap) { resultString, resultMap ->
            runOnUiThread {
                showLoading(false)
                resultTextView.text = resultString
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        classifierHelper.close()
    }
}
