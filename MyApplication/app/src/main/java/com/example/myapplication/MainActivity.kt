package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main)

        val cameraButton: Button = findViewById(R.id.highPlank)
        cameraButton.setOnClickListener {
            toPageCamera()
        }
    }

    fun toPageCamera (){
        val intent = Intent(this, CameraPage::class.java)
        startActivity(intent);

    }
}