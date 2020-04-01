package com.radzievska.oleksandra.androidframework

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import android.widget.Spinner
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.ArFragment
import com.radzievska.oleksandra.androidframework.DrawingViews.DrawView
import com.radzievska.oleksandra.androidframework.Tools.Utils
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.Image
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.radzievska.oleksandra.androidframework.DrawingViews.QRDrawingView
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.TimeUnit


class SceneformActivity : AppCompatActivity() {

    private val TAG = "SceneformActivity"


    private lateinit var arFragment: ArFragment
    private var targetBitmap: Bitmap? = null

    private var lastAnalyzedTimestamp = 0L
    private var currentTimestamp = 0L


    //private lateinit var arcoreFocusOnSpinner: Spinner
    ///private var arcoreFocusOn = "capture"


    private var callbackThread = HandlerThread("callback-worker")
    private lateinit var callbackHandler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sceneform)


        arFragment = supportFragmentManager
            .findFragmentById(R.id.sceneform_fragment) as SceneformArFragment

        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame ?: return

        currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)
        ) {
            var t = Thread(Runnable {
                copyPixelFromView(arFragment.arSceneView)

                Log.i(TAG, "View renderable from image view")
            })
            t.start()
            lastAnalyzedTimestamp = currentTimestamp

        }
    }

    private fun classifySurfaceView(image: Image) {


    }


    private fun copyPixelFromView(view: SurfaceView){
        Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        Log.d(TAG, view.width.toString())
        Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        if(view.width<=0 || view.height<=0){
            return
        }
        var bitmap = Bitmap.createBitmap(
            view!!.width,
            view.height,
            Bitmap.Config.ARGB_8888)
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                Log.i(TAG, "Copying ArFragment view.")
                runQRDetection(bitmap)
               // callback(bitmap)
                Log.i(TAG, "Copied ArFragment view.")

            } else {
                Log.e(TAG, "Failed to copy ArFragment view.")
            }
        }, callbackHandler)
    }

    fun classification(bitmap: Bitmap){
        Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
    }

    private fun runQRDetection(bitmap: Bitmap){
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE,
                FirebaseVisionBarcode.FORMAT_AZTEC)
            .build()

        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        val result = detector.detectInImage(image)
            .addOnSuccessListener { barcodes ->
                Log.d("DETECTED QR", barcodes.toString())

                for (barcode in barcodes){
                    Log.d(TAG, barcode.rawValue)
                }


            }
            .addOnFailureListener {
                Log.d(TAG, "ERROR!!!!!!!!!!!!!!!!!")
            }
    }

    override fun onStop() {
        super.onStop()
        callbackThread.interrupt()
    }

    override fun onDestroy() {
        super.onDestroy()
        callbackThread.quitSafely()
    }
}
