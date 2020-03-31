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
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.radzievska.oleksandra.androidframework.DrawingViews.QRDrawingView
import org.jetbrains.anko.runOnUiThread
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class QRAnalyzer(private val context: Context, private val imageView: ImageView) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L
    lateinit var overlay: Bitmap

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
            if (mediaImage != null) {
                runQRDetection(mediaImage.toBitmap())

            }
            lastAnalyzedTimestamp = currentTimestamp
        }
    }

    private fun runQRDetection(bitmap: Bitmap){
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE,
                FirebaseVisionBarcode.FORMAT_AZTEC)
            .build()

        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        val result = detector.detectInImage(image)
            .addOnSuccessListener { barcodes ->
                Log.d("DETECTED QR", barcodes.toString())

                overlay = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

                val drawingView = QRDrawingView(context, barcodes)
                drawingView.draw(Canvas(overlay))

                context.runOnUiThread {
                    imageView.setImageBitmap(overlay)
                }

//                for (barcode in barcodes) {
//
//                    val bounds = barcode.boundingBox
//                    Log.d("BOUNDS", barcode.boundingBox.toString())
//                    val corners = barcode.cornerPoints
//
//                    val rawValue = barcode.rawValue
//
//                    val valueType = barcode.valueType
//                    // See API reference for complete list of supported types
//                    when (valueType) {
//                        FirebaseVisionBarcode.TYPE_WIFI -> {
//                            val ssid = barcode.wifi!!.ssid
//                            val password = barcode.wifi!!.password
//                            val type = barcode.wifi!!.encryptionType
//                        }
//                        FirebaseVisionBarcode.TYPE_URL -> {
//                            val title = barcode.url!!.title
//                            val url = barcode.url!!.url
//                        }
//                    }
//                }

            }
            .addOnFailureListener {
                Toast.makeText(
                    context, "Oops, something went wrong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}