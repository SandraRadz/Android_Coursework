package com.radzievska.oleksandra.arlabeler

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
import com.radzievska.oleksandra.arlabeler.Tools.SnackbarHelper

open class SceneformArFragment: ArFragment(){
    private val TAG = "SceneformArFragment"
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

}