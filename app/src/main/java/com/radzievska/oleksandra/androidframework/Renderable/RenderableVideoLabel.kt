package com.radzievska.oleksandra.androidframework.Renderable

import com.google.ar.core.Anchor
import com.google.ar.sceneform.ux.ArFragment
import android.view.Gravity
import android.widget.Toast
import java.lang.reflect.Array.setBoolean
import android.R
import android.content.Context
import android.widget.TextView
import com.google.ar.sceneform.rendering.ModelRenderable
import androidx.core.view.accessibility.AccessibilityRecordCompat.setSource
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode


class RenderableVideoLabel(private val context: Context): RenderableLabel{

    var text = "no text"
    var myNode: AnchorNode? = null

    val TAG = "RenderableVideoLabel"

    override fun addLabelToScene(arFragment: ArFragment, anchor: Anchor) {
//        ModelRenderable.builder()
//            .setSource(context, R.raw.my_chroma_key_video)
//            .build()
//            .thenAccept(
//                { renderable ->
//                    videoRenderable = renderable
//                    renderable.getMaterial().setExternalTexture("videoTexture", texture)
//                    renderable.getMaterial().setFloat4("keyColor", CHROMA_KEY_COLOR)
//                    renderable.getMaterial().setBoolean("disableChromaKey", false)
//
//                })
//            .exceptionally(
//                { throwable ->
//                    val toast = Toast.makeText(
//                        context,
//                        "Unable to load video renderable",
//                        Toast.LENGTH_LONG
//                    )
//                    toast.setGravity(Gravity.CENTER, 0, 0)
//                    toast.show()
//                    null
//                })
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        if (myNode!=null){
            fragment.arSceneView.scene.removeChild(myNode)
        }
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        val r = node.renderable as ViewRenderable
        val v = r.view
        val textView = v.findViewById<TextView>(com.radzievska.oleksandra.androidframework.R.id.label_text_view)
        textView.text = text
        myNode  = anchorNode
        fragment.arSceneView.scene.addChild(anchorNode)
    }


    override fun setTextToLabel(text: String) {
        this.text = text
    }
}