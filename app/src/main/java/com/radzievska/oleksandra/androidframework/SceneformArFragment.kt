package com.radzievska.oleksandra.androidframework

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import com.radzievska.oleksandra.androidframework.Tools.SnackbarHelper

open class SceneformArFragment: ArFragment(){
    private val TAG = "SceneformArFragment"
    private val PLANE_FINDING_MODE = Config.PlaneFindingMode.HORIZONTAL
    private val UPDATE_MODE = Config.UpdateMode.LATEST_CAMERA_IMAGE
    private val PLANERENDERER_ENABLED = true
    private val PLANERENDERER_VISIBLE = true
    private val PLANERENDERER_SHADOW_RECEIVER = true
    private val MIN_OPENGL_VERSION = 3.0



    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            SnackbarHelper.getInstance()
                .showError(activity, "Sceneform requires Android N or later")
        }

        val openGlVersionString =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later")
            SnackbarHelper.getInstance()
                .showError(activity, "Sceneform requires OpenGL ES 3.0 or later")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }


    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)

        // Use setFocusMode to configure auto-focus.
        config.focusMode = Config.FocusMode.AUTO

        return config
    }

//    override fun getSessionConfiguration(session: Session?): Config {
//        Log.i(TAG, "setup new session")
//        planeDiscoveryController.hide()
//        planeDiscoveryController.setInstructionView(null)
//
//        val config = Config(session)
//        config.planeFindingMode = PLANE_FINDING_MODE
//        config.updateMode = UPDATE_MODE
//        // config.focusMode = Config.FocusMode.AUTO
//        session?.configure(config)
//        this.arSceneView.setupSession(session)
//        this.arSceneView.planeRenderer.isEnabled = PLANERENDERER_ENABLED
//        this.arSceneView.planeRenderer.isVisible = PLANERENDERER_VISIBLE
//        this.arSceneView.planeRenderer.isShadowReceiver = PLANERENDERER_SHADOW_RECEIVER
//
//        if (session != null) {
//            activity.let { it as? SceneformActivity }
//        }
//        Log.i(TAG, config.toString())
//        return config
//    }



}