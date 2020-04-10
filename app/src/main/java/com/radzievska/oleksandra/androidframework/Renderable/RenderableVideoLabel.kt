package com.radzievska.oleksandra.androidframework.Renderable

import android.annotation.SuppressLint
import com.google.ar.core.Anchor
import com.google.ar.sceneform.ux.ArFragment
import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.radzievska.oleksandra.androidframework.R


class RenderableVideoLabel: RenderableLabel{

    var text = "no text"
    var myNode: AnchorNode? = null

    val TAG = "RenderableVideoLabel"

    override fun addLabelToScene(arFragment: ArFragment, anchor: Anchor) {
        ViewRenderable
            .builder()
            .setView(arFragment.context, R.layout.label_video_view)
            .build()
            .thenAccept {
                it.isShadowCaster = true
                it.isShadowReceiver = true
                addNodeToScene(arFragment, anchor, it)
                Log.i(TAG, "Placing Video object")
            }
            .exceptionally {
                val builder = AlertDialog.Builder(arFragment.context)
                builder.setMessage(it.message).setTitle("${TAG}: Error")
                builder.create().show()
                return@exceptionally null
            }
    }

    @SuppressLint("ResourceType")
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        if (myNode!=null){
            fragment.arSceneView.scene.removeChild(myNode)
        }
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)

        val r = renderable as ViewRenderable
        val v = r.view

        val videoView = v.findViewById<VideoView>(R.id.label_video_view)
        Log.d(TAG, text)
        videoView.setVideoURI(Uri.parse(text))
        videoView.setMediaController(MediaController(fragment.context))
        videoView.start()

        myNode  = anchorNode
        fragment.arSceneView.scene.addChild(anchorNode)
    }


    override fun setTextToLabel(text: String) {
        this.text = text
    }
}