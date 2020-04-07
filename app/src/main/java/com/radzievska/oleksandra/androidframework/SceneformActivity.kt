package com.radzievska.oleksandra.androidframework

import android.content.Intent
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
import android.widget.ImageView
import com.google.ar.core.Session
import com.google.ar.sceneform.rendering.ViewRenderable
import com.radzievska.oleksandra.androidframework.Analyzers.Analyzer
import com.radzievska.oleksandra.androidframework.Analyzers.ObjectSceneformAnalyzer
import com.radzievska.oleksandra.androidframework.Analyzers.QRSceneformAnalyzer
import com.radzievska.oleksandra.androidframework.Tools.CameraPermissionHelper
import java.util.concurrent.TimeUnit


class SceneformActivity : AppCompatActivity() {

    private val TAG = "SceneformActivity"


    private lateinit var arFragment: SceneformArFragment
    private var targetBitmap: Bitmap? = null


    private var lastAnalyzedTimestamp = 0L
    private var currentTimestamp = 0L
    private lateinit var detector : Analyzer
    private var model = "qq"
    private var resource: Int = R.raw.andy
    // private lateinit var drawable_image_view: ImageView

    private var callbackThread = HandlerThread("callback-worker")
    private lateinit var callbackHandler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sceneform)

        // drawable_image_view = findViewById(R.id.imageView)

        arFragment = supportFragmentManager
            .findFragmentById(R.id.sceneform_fragment) as SceneformArFragment

   }

    override fun onResume() {
        super.onResume()

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)

        detector = if(model != null){
            ObjectSceneformAnalyzer(this@SceneformActivity, arFragment, resource, model)
        } else{
            QRSceneformAnalyzer(this@SceneformActivity, arFragment, resource)
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

    override fun onPause() {
        super.onPause()
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
