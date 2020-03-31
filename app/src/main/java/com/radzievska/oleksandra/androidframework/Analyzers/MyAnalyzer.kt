package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.radzievska.oleksandra.androidframework.DrawingViews.LabelDrawingView
import com.radzievska.oleksandra.androidframework.DrawingViews.QRDrawingView
import com.radzievska.oleksandra.androidframework.R
import org.jetbrains.anko.runOnUiThread
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MyAnalyzer(private val context: Context, private val imageView: ImageView) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L
    private var andyRenderable: ModelRenderable? = null
    private var resource = R.raw.andy
    lateinit var overlay: Bitmap
    // todo label to rendering
    // todo model
    // todo qr/model/ar



    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

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
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)) {
                val mediaImage = imageProxy?.image
                val imageRotation = degreesToFirebaseRotation(degrees)
                if (mediaImage != null) {
                    runLocalModel(mediaImage, imageRotation)

                }
            lastAnalyzedTimestamp = currentTimestamp
        }
    }

    private fun runLocalModel(mediaImage: Image, rotation: Int){

        val localModel = FirebaseAutoMLLocalModel.Builder()
            .setAssetFilePath("birds/manifest.json")
            .build()

        val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)  // Evaluate your model in the Firebase console
            // to determine an appropriate value.
            .build()
        val labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options)

        val image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)

        labeler.processImage(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    val confidence = label.confidence
                    val text = label.text
                    val entityId = label.entityId
                    Log.d("LABELS!!!!!!!!!!!!", "$text $confidence")


                    if(confidence > 0.7) {
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error:(", Toast.LENGTH_SHORT).show()
            }
    }

    private fun renderModel(resource: Int) {
        ModelRenderable.builder()
            .setSource(context, resource)
            .build()
            .thenAccept { renderable -> andyRenderable = renderable }
            .exceptionally { throwable ->
                Log.e(TAG, "Unable to load Renderable.", throwable)
                null
            }
    }
}