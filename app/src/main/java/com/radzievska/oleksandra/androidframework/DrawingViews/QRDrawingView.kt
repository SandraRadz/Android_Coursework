package com.radzievska.oleksandra.androidframework.DrawingViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode


class QRDrawingView(context: Context, var visionObjects: List<FirebaseVisionBarcode>) : View(context) {

    val MAX_FONT_SIZE = 96F

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        for (item in visionObjects) {
            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = item.boundingBox
            if (box != null) {
                canvas.drawRect(box, pen)
            }

            // Draw result category, and confidence
            val tags: MutableList<String> = mutableListOf()
            tags.add("${item.rawValue}")
           

            var tagSize = Rect(0, 0, 0, 0)
            var maxLen = 0
            var index: Int = -1

            for ((idx, tag) in tags.withIndex()) {
                if (maxLen < tag.length) {
                    maxLen = tag.length
                    index = idx
                }
            }

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = MAX_FONT_SIZE
            pen.getTextBounds(tags[index], 0, tags[index].length, tagSize)
            val fontSize: Float = pen.textSize * (box?.width() ?: 500) / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = ((box?.width() ?: 500) - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F

            // draw tags onto bitmap (bmp is in upside down format)
            for ((idx, txt) in tags.withIndex()) {
                if (box != null) {
                    canvas.drawText(
                        txt, box.left + margin,
                        box.top + tagSize.height().times(idx + 1.0F), pen
                    )
                }
                else{
                    canvas.drawText(
                        txt, 100F,
                        100F + tagSize.height().times(idx + 1.0F), pen
                    )
                }
            }
        }
    }
}