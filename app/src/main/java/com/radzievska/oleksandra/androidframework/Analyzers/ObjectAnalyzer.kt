package com.radzievska.oleksandra.androidframework

import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.radzievska.oleksandra.androidframework.DrawingViews.LabelDrawingView
import org.jetbrains.anko.runOnUiThread
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ObjectAnalyzer(private val context: Context, private val imageView: ImageView) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L
    private var currentTimestamp = 0L
    lateinit var overlay: Bitmap

    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun analyze(imageProxy: ImageProxy, degrees: Int) {
        currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)) {
            val mediaImage = imageProxy?.image
            if (mediaImage != null) {
                runObjectDetection(mediaImage.toBitmap())

            }
            lastAnalyzedTimestamp = currentTimestamp
        }
    }

    private fun runObjectDetection(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .build()
        val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)


        detector.processImage(image)
            .addOnSuccessListener {
                Log.d("DETECTED OBJECTS", it.toString())

                overlay = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

                val drawingView = LabelDrawingView(context, it)
                drawingView.draw(Canvas(overlay))

                context.runOnUiThread {
                    imageView.setImageBitmap(overlay)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context, "Oops, something went wrong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}