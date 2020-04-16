package com.radzievska.oleksandra.androidframework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.sceneform.FrameTime
import com.radzievska.oleksandra.androidframework.Analyzers.Analyzer
import com.radzievska.oleksandra.androidframework.Renderable.Renderable3DLabel
import com.radzievska.oleksandra.androidframework.Renderable.RenderableTextLabel
import com.radzievska.oleksandra.androidframework.Renderable.RenderableVideoLabel
import com.radzievska.oleksandra.androidframework.Tools.CameraPermissionHelper
import com.radzievska.oleksandra.arlabeler.SceneformArFragment
import java.util.concurrent.TimeUnit


class SceneformActivity : AppCompatActivity() {

    private val TAG = "SceneformActivity"


    private lateinit var arFragment: SceneformArFragment


    private var lastAnalyzedTimestamp = 0L
    private var currentTimestamp = 0L
    private lateinit var arLabeler : ARLabeler

    private lateinit var detector : Analyzer

    val model = "birds/manifest.json"

    //private var resource: Int = R.raw.andy

    // Alex “SAFFY” Safayan
    private var resource: Int = R.raw.model


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sceneform)

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
        arLabeler = ARLabeler(arFragment, RenderableTextLabel(), model)
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
                arLabeler.runLabeling(arFragment.arSceneView)
                Log.i(TAG, "View renderable from image view")
            })
            t.start()
            lastAnalyzedTimestamp = currentTimestamp
        }
    }

    override fun onPause() {
        super.onPause()
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
