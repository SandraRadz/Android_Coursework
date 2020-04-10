package com.radzievska.oleksandra.androidframework.Renderable

import com.google.ar.core.Anchor
import com.google.ar.sceneform.ux.ArFragment

interface  RenderableLabel{

      fun addLabelToScene(arFragment: ArFragment, anchor: Anchor)

      fun setTextToLabel(text: String)



}