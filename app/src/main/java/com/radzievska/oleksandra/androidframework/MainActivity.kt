package com.radzievska.oleksandra.androidframework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import androidx.lifecycle.LifecycleOwner

class MainActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openARActivity(view: View) {
        intent = Intent(this, ARActivity()::class.java)
        startActivity(intent)
    }

    fun openAugmentedImagesActivity(view: View) {
        intent = Intent(this, AugmentedImagesActivity()::class.java)
        startActivity(intent)
    }

    fun openSceneformActivity(view: View) {
        intent = Intent(this, SceneformActivity()::class.java)
        startActivity(intent)
    }


}
