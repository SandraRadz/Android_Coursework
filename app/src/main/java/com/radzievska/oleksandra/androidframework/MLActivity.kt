package com.radzievska.oleksandra.androidframework

import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import com.radzievska.oleksandra.androidframework.Tools.CameraPermissionHelper
import java.util.concurrent.Executors

class MLActivity : AppCompatActivity() {

    private lateinit var viewFinder: TextureView
    lateinit var imageView: ImageView
    private val objectDetection: Boolean = false
    private var resource = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml)

        viewFinder = findViewById(R.id.view_finder)
        imageView = findViewById(R.id.imageView)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        viewFinder.post { startCamera() }
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }


    private val executor = Executors.newSingleThreadExecutor()

    private fun startCamera() {

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(1040, 1040))
        }.build()

        val preview = Preview(previewConfig)


        preview.setOnPreviewOutputUpdateListener {

            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = when {
            resource != null -> ImageAnalysis(analyzerConfig).apply {
                setAnalyzer(executor, ObjectAnalyzer(this@MLActivity, imageView))
            }
            objectDetection -> ImageAnalysis(analyzerConfig).apply {
                setAnalyzer(executor, ObjectAnalyzer(this@MLActivity, imageView))
            }
            else -> ImageAnalysis(analyzerConfig).apply {
                setAnalyzer(executor, QRAnalyzer(this@MLActivity, imageView))
            }
        }


        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
    }
    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this,
                "Permissions not granted by the user.",
                Toast.LENGTH_SHORT).show()
            finish()
        }
        viewFinder.post { startCamera() }
    }

}
