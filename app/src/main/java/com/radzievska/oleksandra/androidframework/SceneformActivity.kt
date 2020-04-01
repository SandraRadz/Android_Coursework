package com.radzievska.oleksandra.androidframework

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import android.media.Image
import com.radzievska.oleksandra.androidframework.Analyzers.Analyzer
import com.radzievska.oleksandra.androidframework.Analyzers.ObjectSceneformAnalyzer
import com.radzievska.oleksandra.androidframework.Analyzers.QRSceneformAnalyzer
import java.util.concurrent.TimeUnit


class SceneformActivity : AppCompatActivity() {

    private val TAG = "SceneformActivity"


    private lateinit var arFragment: ArFragment
    private var targetBitmap: Bitmap? = null

    private var lastAnalyzedTimestamp = 0L
    private var currentTimestamp = 0L
    private lateinit var detector : Analyzer
    private var model: String? = null
    private var image_object: Int? = null

    private var callbackThread = HandlerThread("callback-worker")
    private lateinit var callbackHandler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sceneform)


        arFragment = supportFragmentManager
            .findFragmentById(R.id.sceneform_fragment) as SceneformArFragment

        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)

        detector = if(model != null){
            ObjectSceneformAnalyzer(this@SceneformActivity, image_object, model)
        } else{
            QRSceneformAnalyzer(this@SceneformActivity, image_object)
        }

    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame ?: return

        currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)
        ) {
            var t = Thread(Runnable {
                copyPixelFromView(arFragment.arSceneView)

                Log.i(TAG, "View renderable from image view")
            })
            t.start()
            lastAnalyzedTimestamp = currentTimestamp

        }
    }

    private fun copyPixelFromView(view: SurfaceView){
        if(view.width<=0 || view.height<=0){
            return
        }
        var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                detector.runDetection(bitmap)
            } else {
                Log.e(TAG, "Failed to copy ArFragment view.")
            }
        }, callbackHandler)
    }

    override fun onStop() {
        super.onStop()
        callbackThread.interrupt()
    }

    override fun onDestroy() {
        super.onDestroy()
        callbackThread.quitSafely()
    }
}
