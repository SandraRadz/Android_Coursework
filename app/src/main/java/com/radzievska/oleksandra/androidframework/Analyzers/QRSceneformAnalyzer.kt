package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.radzievska.oleksandra.androidframework.R
import com.radzievska.oleksandra.androidframework.Renderable.Renderable3DLabel
import com.radzievska.oleksandra.androidframework.Renderable.RenderableLabel
import com.radzievska.oleksandra.androidframework.Renderable.RenderableTextLabel
import org.jetbrains.anko.runOnUiThread

class QRSceneformAnalyzer(context: Context, private val arFragment: ArFragment, private val resource: Int?): Analyzer{

    val TAG = "QRSceneformAnalyzer"
    lateinit var overlay: Bitmap
    var session : Session? = null


    var draw: RenderableLabel

    init {
        draw = if (resource != null ){
            Renderable3DLabel(context, resource)
        } else{
            RenderableTextLabel(context)
        }
    }
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

                if (barcodes.size>0){
                    session = arFragment.arSceneView.session
                    val item = barcodes[0]
                    val pos = arFragment.arSceneView.arFrame?.camera?.displayOrientedPose?.compose(
                        Pose.makeTranslation(0F, 0F, -0.8f))
                    item.rawValue?.let { draw.setTextToLabel("$it\nQR") }
                    val anchor = session?.createAnchor(pos)

                    if (anchor != null) {
                        draw.setLabel(arFragment, anchor)
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "ERROR!!!!!!!!!!!!!!!!!")
            }
    }

}