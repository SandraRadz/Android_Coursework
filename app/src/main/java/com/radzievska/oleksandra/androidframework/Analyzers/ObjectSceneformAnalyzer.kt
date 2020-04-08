package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.radzievska.oleksandra.androidframework.Renderable.RenderableTextLabel
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.radzievska.oleksandra.androidframework.Renderable.Renderable3DLabel
import com.radzievska.oleksandra.androidframework.Renderable.RenderableLabel



class ObjectSceneformAnalyzer(context: Context, private val arFragment: ArFragment, resource: Int?, private val model: String?) : Analyzer{

    companion object {
        val categoryNames: Map<Int, String> = mapOf(
            FirebaseVisionObject.CATEGORY_UNKNOWN to "Unknown",
            FirebaseVisionObject.CATEGORY_HOME_GOOD to "Home Goods",
            FirebaseVisionObject.CATEGORY_FASHION_GOOD to "Fashion Goods",
            FirebaseVisionObject.CATEGORY_FOOD to "Food",
            FirebaseVisionObject.CATEGORY_PLACE to "Place",
            FirebaseVisionObject.CATEGORY_PLANT to "Plant"
        )
    }

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
                    val pos = arFragment.arSceneView.arFrame?.camera?.displayOrientedPose?.compose(Pose.makeTranslation(0F, 0F, -0.8f))

                    if(item.classificationCategory != FirebaseVisionObject.CATEGORY_UNKNOWN){
                        categoryNames[item.classificationCategory]?.let { it1 -> draw.setTextToLabel("$it1\n${item.classificationConfidence!!.times(100).toInt()}%") }
                    }
                    else{
                        (item.classificationCategory != FirebaseVisionObject.CATEGORY_UNKNOWN)
                    }
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