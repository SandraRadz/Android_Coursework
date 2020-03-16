package com.radzievska.oleksandra.androidframework

import android.content.Context
import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class LuminosityAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    /**
     * Helper extension function used to extract a byte array from an
     * image plane buffer
     */
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(imageProxy: ImageProxy, degrees: Int) {
//        val currentTimestamp = System.currentTimeMillis()
//        // Calculate the average luma no more often than every second
//        if (currentTimestamp - lastAnalyzedTimestamp >=
//            TimeUnit.SECONDS.toMillis(1)) {
//            // Since format in ImageAnalysis is YUV, image.planes[0]
//            // contains the Y (luminance) plane
//            val buffer = image.planes[0].buffer
//            // Extract image data from callback object
//            val data = buffer.toByteArray()
//            // Convert the data into an array of pixel values
//            val pixels = data.map { it.toInt() and 0xFF }
//            // Compute average luminance for the image
//            val luma = pixels.average()
//            // Log the new luma value
//            Log.d("CameraXApp", "Average luminosity: $luma")
//            // Update timestamp of last analyzed frame
//            lastAnalyzedTimestamp = currentTimestamp
//        }
        val mediaImage = imageProxy?.image
        val imageRotation = degreesToFirebaseRotation(degrees)
        if (mediaImage != null) {
            runLocalModel(mediaImage, imageRotation)
           // val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            // Pass image to an ML Kit Vision API
            // ...
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
                        Log.d("LABELS!!!!!!!!!!!!", "$text $confidence")
                    // todo print text + add button to activity with AR
                   // if(confidence > 0.7) {
                   //     Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                   // }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error:(", Toast.LENGTH_SHORT).show()
            }
    }
}