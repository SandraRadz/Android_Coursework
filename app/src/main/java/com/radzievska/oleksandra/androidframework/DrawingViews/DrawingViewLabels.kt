package com.radzievska.oleksandra.androidframework.DrawingViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.google.firebase.ml.vision.objects.FirebaseVisionObject


/**
 * DrawingViewLabels class:
 *    onDraw() function implements drawing
 *     - boundingBox
 *     - Category
 *     - Confidence ( if Category is not CATEGORY_UNKNOWN )
 */
class DrawingViewLabels(context: Context, var visionObjects: List<FirebaseVisionObject>) : View(context) {

    companion object {
        // mapping table for category to strings: drawing strings
        val categoryNames: Map<Int, String> = mapOf(
            FirebaseVisionObject.CATEGORY_UNKNOWN to "Unknown",
            FirebaseVisionObject.CATEGORY_HOME_GOOD to "Home Goods",
            FirebaseVisionObject.CATEGORY_FASHION_GOOD to "Fashion Goods",
            FirebaseVisionObject.CATEGORY_FOOD to "Food",
            FirebaseVisionObject.CATEGORY_PLACE to "Place",
            FirebaseVisionObject.CATEGORY_PLANT to "Plant"
        )
    }

    val MAX_FONT_SIZE = 96F

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pen = Paint()
        val margin = 5

        //for (item in visionObjects) {
        if(visionObjects.isNotEmpty()){
            // todo check if it's object with the highest confidence
            val item = visionObjects[0]
            if (item.classificationCategory != FirebaseVisionObject.CATEGORY_UNKNOWN) {
                val confidence = item.classificationConfidence!!.times(100).toInt()
                if (confidence > 85) {
                    val box = item.boundingBox
                    val top = box.top
                    val left = box.left

                    val str = "Category: ${categoryNames[item.classificationCategory]}"+"\n"+
                            "Confidence: ${item.classificationConfidence!!.times(100).toInt()}%"


                    val fm = Paint.FontMetrics()
                    pen.color = Color.BLACK
                    pen.textSize = 18.0f
                    pen.getFontMetrics(fm)



                    pen.color = Color.WHITE
                    pen.style = Paint.Style.FILL_AND_STROKE
                    canvas.drawRect((top-margin).toFloat(), (left-margin).toFloat(),
                        left.toFloat()+pen.measureText(str) + margin, (top+10*margin).toFloat(), pen)

                    pen.color = Color.BLACK
                    canvas.drawText(str, top.toFloat(), left.toFloat(), pen)
                }
            }
        }
    }

    private fun drawLabel(){

    }
}