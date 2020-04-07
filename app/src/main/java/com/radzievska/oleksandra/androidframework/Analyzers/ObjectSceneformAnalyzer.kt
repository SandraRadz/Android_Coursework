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
import com.radzievska.oleksandra.androidframework.Renderable.RenderableLabel

// todo delete previous object, add trecable
class ObjectSceneformAnalyzer(private val context: Context, private val arFragment: ArFragment, private val resource: Int?, private val model: String?) : Analyzer{

    val TAG = "ObjectScenefonmAnalyzer"
    lateinit var overlay: Bitmap
    var session :Session? = null

    var draw: RenderableLabel

    init {
        draw = if (resource != null ){
            Renderable3DLabel(context, resource)
        } else{
            RenderableTextLabel(context)
        }
        //draw = RenderableTextLabel(context)
    }

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

                if (it.size>0){
                    session = arFragment.arSceneView.session
                    val item = it[0]
                    val pos = arFragment.arSceneView.arFrame?.camera?.pose?.compose(Pose.makeTranslation(0F, 0F, -0.8f))
                    draw.setTextToLabel(item.classificationCategory.toString())
                    val anchor = session?.createAnchor(pos)

                    if (anchor != null) {
                        draw.setLabel(arFragment, anchor)
                    }
                }
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