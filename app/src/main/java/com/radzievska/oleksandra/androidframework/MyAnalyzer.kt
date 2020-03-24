package com.radzievska.oleksandra.androidframework

import android.content.Context
import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class MyAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
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
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)) {
                val mediaImage = imageProxy?.image
                val imageRotation = degreesToFirebaseRotation(degrees)
                if (mediaImage != null) {
                    trackObject(mediaImage, imageRotation)

                    // val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                    // Pass image to an ML Kit Vision API
                    // ...

                }
            lastAnalyzedTimestamp = currentTimestamp
        }
    }

    private fun trackObject(mediaImage: Image, rotation: Int){
        // Live detection and tracking
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
            .enableClassification()  // Optional
            .build()

        val objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)
        //val objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

        val image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)


        objectDetector.processImage(image)
            .addOnSuccessListener { detectedObjects ->
                Log.d("SUCCESS!!!!!!!!!!!!", "!!!!!!!!!!!!")
                for (obj in detectedObjects) {
                    val id = obj.trackingId       // A number that identifies the object across images
                    val bounds = obj.boundingBox  // The object's position in the image

                    // If classification was enabled:
                    val category = obj.classificationCategory
                    val confidence = obj.classificationConfidence
                    Log.d("!!!!!!!!!!!!!!!!!!", ""+category)
                }
            }
            .addOnFailureListener { e ->
                Log.d("FAILED!!!!!!!!!!!!", "!!!!!!!!!!!!")
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

    // todo finish later
    private fun runRemoteModel(){
        // Firebase
        val remoteModel = FirebaseAutoMLRemoteModel.Builder("Birds_2020311231646").build()
        val conditions = FirebaseModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                // Success.
            }
    }
}