package com.radzievska.oleksandra.androidframework.Analyzers

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView

interface Analyzer{

    fun runDetection(bitmap: Bitmap)

}