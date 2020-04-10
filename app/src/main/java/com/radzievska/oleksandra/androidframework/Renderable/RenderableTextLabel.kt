package com.radzievska.oleksandra.androidframework.Renderable

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.widget.TextView
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.radzievska.oleksandra.androidframework.R

class RenderableTextLabel: RenderableLabel {

    var text = "no text"
    var myNode: AnchorNode? = null

    val TAG = "RenderableTextLabel"


    override fun addLabelToScene(arFragment: ArFragment, anchor: Anchor) {
        ViewRenderable
            .builder()
            .setView(arFragment.context, R.layout.label_text_view)
            .build()
            .thenAccept {
                it.isShadowCaster = true
                it.isShadowReceiver = true
                addNodeToScene(arFragment, anchor, it)
                Log.i(TAG, "Placing Text object")
            }
            .exceptionally {
                val builder = AlertDialog.Builder(arFragment.context)
                builder.setMessage(it.message).setTitle("${TAG}: Error")
                builder.create().show()
                return@exceptionally null
            }
    }

    override fun setTextToLabel(text: String){
        this.text = text
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
        val textView = v.findViewById<TextView>(R.id.label_text_view)
        textView.text = text
        myNode  = anchorNode
        fragment.arSceneView.scene.addChild(anchorNode)
    }


}