package com.radzievska.oleksandra.androidframework

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.radzievska.oleksandra.androidframework.Analyzers.Analyzer
import com.radzievska.oleksandra.androidframework.Analyzers.ObjectSceneformAnalyzer
import com.radzievska.oleksandra.androidframework.Analyzers.QRSceneformAnalyzer
import com.radzievska.oleksandra.androidframework.Renderable.RenderableLabel
import com.radzievska.oleksandra.androidframework.Renderable.RenderableTextLabel

class ARLabeler (context: Context, arFragment: ArFragment, model: String?) {

    private var label : RenderableLabel
    private var analyzer: Analyzer
    private val TAG = "ARLabeler"

    private var callbackThread = HandlerThread("callback-worker")
    private var callbackHandler: Handler

    init {
        label = RenderableTextLabel(context)

        analyzer = if (model!=null){
            val localModel = FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath(model).build()
            ObjectSceneformAnalyzer(context, arFragment, localModel, null)
        } else{
            QRSceneformAnalyzer(context, arFragment)
        }

        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)
    }

    fun setLabel(renderable: RenderableLabel){
        this.label = renderable
    }

    fun runLabeling(view: SurfaceView){
        if(view.width<=0 || view.height<=0){
            return
        }
        var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                analyzer.runDetection(bitmap)

            } else {
                Log.e(TAG, "Failed to copy ArFragment view.")
            }
        }, callbackHandler)
    }
}