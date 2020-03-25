package com.radzievska.oleksandra.androidframework

import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.*
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_detect_object.*
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions

class DetectObjectActivity: AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val ODT_PERMISSIONS_REQUEST: Int = 1
        const val ODT_REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var outputFileUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect_object)
        captureImageFab.setOnClickListener {

            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePhotoIntent.resolveActivity(packageManager) != null) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, "MLKit_codelab")
                outputFileUri = contentResolver
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                startActivityForResult(takePhotoIntent, ODT_REQUEST_IMAGE_CAPTURE)
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {

            captureImageFab.isEnabled = false
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                ODT_PERMISSIONS_REQUEST
            )
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ODT_REQUEST_IMAGE_CAPTURE &&
            resultCode == Activity.RESULT_OK) {

            val image = getCapturedImage()

            // display capture image
            imageView.setImageBitmap(image)

            // run through ODT and display result
            runObjectDetection(image)
        }
    }

    /**
     * MLKit Object Detection Function
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        // Step 1: create MLKit's VisionImage object
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // Step 2: acquire detector object
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

        // Step 3: feed given image to detector and setup callback
        detector.processImage(image)
            .addOnSuccessListener {
                // Task completed successfully
                // Post-detection processing : draw result

                val drawingView = DrawingView(applicationContext, it)
                drawingView.draw(Canvas(bitmap))
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            }
            .addOnFailureListener {
                // Task failed with an exception
                Toast.makeText(
                    baseContext, "Oops, something went wrong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * getCapturedImage():
     *     Decodes and center crops the captured image from camera.
     */
    private fun getCapturedImage(): Bitmap {

        val srcImage = FirebaseVisionImage
            .fromFilePath(baseContext, outputFileUri).bitmap

        // crop image to match imageView's aspect ratio
        val scaleFactor = Math.min(
            srcImage.width / imageView.width.toFloat(),
            srcImage.height / imageView.height.toFloat()
        )

        val deltaWidth = (srcImage.width - imageView.width * scaleFactor).toInt()
        val deltaHeight = (srcImage.height - imageView.height * scaleFactor).toInt()

        val scaledImage = Bitmap.createBitmap(
            srcImage, deltaWidth / 2, deltaHeight / 2,
            srcImage.width - deltaWidth, srcImage.height - deltaHeight
        )
        srcImage.recycle()
        return scaledImage

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            ODT_PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    captureImageFab.isEnabled = true
                }
            }
        }
    }
}

