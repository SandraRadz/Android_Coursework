package com.radzievska.oleksandra.androidframework.Analyzers

import android.graphics.Bitmap
import android.util.Log
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.radzievska.oleksandra.androidframework.Renderable.RenderableLabel


class QRSceneformAnalyzer(private val arFragment: ArFragment, private val drawLabel: RenderableLabel): Analyzer{

    val TAG = "QRSceneformAnalyzer"
    var session : Session? = null
    var detected: Boolean = false


    override fun runDetection(bitmap: Bitmap) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE,
                FirebaseVisionBarcode.FORMAT_AZTEC)
            .build()

        if (!detected){
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
            detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->
                    Log.d("DETECTED QR", barcodes.toString())

                    if (barcodes.size>0){
                        session = arFragment.arSceneView.session
                        val item = barcodes[0]
                        val pos = arFragment.arSceneView.arFrame?.camera?.displayOrientedPose?.compose(
                            Pose.makeTranslation(0F, 0F, -0.8f))
                        item.rawValue?.let { drawLabel.setTextToLabel("$it\nQR") }

                        val valueType = item.valueType
                        if (valueType == FirebaseVisionBarcode.TYPE_URL){
                            item.url?.url?.let { drawLabel.setTextToLabel(it) }
                        }

                        val anchor = session?.createAnchor(pos)

                        if (anchor != null) {
                            detected = true
                            drawLabel.addLabelToScene(arFragment, anchor)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "ERROR!")
                }
        }

    }

}