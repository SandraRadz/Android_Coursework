package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.radzievska.oleksandra.androidframework.Renderable.RenderableTextLabel
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
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

    val localModel = FirebaseAutoMLLocalModel.Builder()
        .setAssetFilePath("birds/manifest.json")
        .build()

    var draw: RenderableLabel = if (resource != null ){
        Renderable3DLabel(context, resource)
    } else{
        RenderableTextLabel(context)
    }

    var detectedBitmap: Bitmap? = null

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
                    val item = it[0]
                    detectedBitmap = makeBitmapFromObject(item,
                        image,
                        null,
                        true)

                    classifyFromDetection(detectedBitmap!!)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "ERROR! Detection")
//                Toast.makeText(
//                    context, "Oops, something went wrong!",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
    }


    fun makeBitmapFromObject(firebaseVisionObject: FirebaseVisionObject,
                             firebaseVisionImage: FirebaseVisionImage,
                             matrix: Matrix?=null,
                             filter: Boolean=true): Bitmap{
        return Bitmap.createBitmap(
            firebaseVisionImage.bitmap,
            firebaseVisionObject.boundingBox.left,
            firebaseVisionObject.boundingBox.top,
            firebaseVisionObject.boundingBox.right - firebaseVisionObject.boundingBox.left,
            firebaseVisionObject.boundingBox.bottom - firebaseVisionObject.boundingBox.top,
            matrix,
            filter
        )
    }

    private fun classifyFromDetection(detectedBitmap: Bitmap){
        val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel).build()
        val labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options)

        val detectedImage = FirebaseVisionImage.fromBitmap(detectedBitmap)

        labeler.processImage(detectedImage)
            .addOnSuccessListener { labels ->
                if (labels.size>0) {
                    val label = labels[0]
                    Log.d("LABELS!!!!!!!!!!!!", labels.toString())
                    session = arFragment.arSceneView.session

                    val pos = arFragment.arSceneView.arFrame?.camera?.displayOrientedPose?.compose(Pose.makeTranslation(0F, 0F, -0.8f))

                   draw.setTextToLabel("${label.text}\n${label.confidence.times(100).toInt()}%")
                    val anchor = session?.createAnchor(pos)

                    if (anchor != null) {
                        draw.setLabel(arFragment, anchor)
                    }

                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "ERROR! Classification")
                //Toast.makeText(context, "Error:(", Toast.LENGTH_SHORT).show()
            }

    }

}