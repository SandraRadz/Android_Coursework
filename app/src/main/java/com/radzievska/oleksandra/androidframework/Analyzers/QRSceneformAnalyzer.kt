package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.radzievska.oleksandra.androidframework.R
import org.jetbrains.anko.runOnUiThread

class QRSceneformAnalyzer(private val context: Context, private val arFragment: Fragment, private val image_object: Int?): Analyzer{

    val TAG = "QRSceneformAnalyzer"
    lateinit var overlay: Bitmap

    // private var drawable_image_view = context.findViewById(R.id.imageView)

    override fun runDetection(bitmap: Bitmap) {
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

//                val node = AugmentedImageNode(context)
//                node.image = augmentedImage
//                arFragment.getArSceneView().getScene().addChild(node)
//                overlay = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

//                val drawingView = QRDrawingView(context, barcodes)
//                drawingView.draw(Canvas(overlay))

//                context.runOnUiThread {
//                    imageView.setImageBitmap(overlay)
//                }
            }
            .addOnFailureListener {
                Log.d(TAG, "ERROR!!!!!!!!!!!!!!!!!")
            }
    }

}