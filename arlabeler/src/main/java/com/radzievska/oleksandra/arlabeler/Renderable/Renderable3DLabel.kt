package com.radzievska.oleksandra.androidframework.Renderable

import android.app.AlertDialog
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class Renderable3DLabel (private val resource: Int) : RenderableLabel{

    override fun setTextToLabel(text: String) {
    }

    val TAG = "Renderable3DLabel"

    var myNode: AnchorNode? = null


    override fun addLabelToScene(arFragment: ArFragment, anchor: Anchor) {
        ModelRenderable
            .builder()
            .setSource(arFragment.context, resource)
            .build()
            .thenAccept {
                it.isShadowCaster = true
                it.isShadowReceiver = true
                addNodeToScene(arFragment, anchor, it)
                Log.i(TAG, "Placing AR object")
            }
            .exceptionally {
                val builder = AlertDialog.Builder(arFragment.context)
                builder.setMessage(it.message).setTitle("${TAG}: Error")
                builder.create().show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        if (myNode!=null){
            fragment.arSceneView.scene.removeChild(myNode)
            }
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        myNode  = anchorNode
        fragment.arSceneView.scene.addChild(anchorNode)
    }

}