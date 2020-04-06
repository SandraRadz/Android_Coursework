package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.ImageView
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.radzievska.oleksandra.androidframework.DrawingViews.LabelDrawingView
import com.radzievska.oleksandra.androidframework.Renderable.RenderableTextLabel
import org.jetbrains.anko.runOnUiThread
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView
import com.radzievska.oleksandra.androidframework.Renderable.Renderable3DLabel


class ObjectSceneformAnalyzer(private val context: Context, private val arFragment: ArFragment, private val image_object: Int?, private val model: String?) : Analyzer{

    val TAG = "ObjectScenefonmAnalyzer"
    lateinit var overlay: Bitmap

    override fun runDetection(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .build()
        val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)


        detector.processImage(image)
            .addOnSuccessListener {
                Log.d("DETECTED OBJECTS", it.toString())

                var draw = Renderable3DLabel(context)

                val pos = floatArrayOf(0f, 0f, -1f)
                val rotation = floatArrayOf(0f, 0f, 0f, 1f)

                val mCameraRelativePose = Pose.makeTranslation(0.0f, 0.0f, -0.5f)

                var session = arFragment.arSceneView.session

                val anchor = session?.createAnchor(Pose(pos, rotation))

                if (anchor != null) {
                    draw.setLabel(arFragment, anchor)
                }
//
//                overlay = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//
//                val drawingView = LabelDrawingView(context, it)
//                drawingView.draw(Canvas(overlay))
//
//                context.runOnUiThread {
//                    imageView.setImageBitmap(overlay)
//                }
            }
            .addOnFailureListener {
                Log.d(TAG, "ERROR!!!!!!!!!!!!!!!!!")
//                Toast.makeText(
//                    context, "Oops, something went wrong!",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
    }

}