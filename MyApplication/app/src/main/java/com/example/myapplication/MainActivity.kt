package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main)

        val highPlankBtn: Button = findViewById(R.id.highPlank)
        val lowSidePlankBtn: Button = findViewById(R.id.lowSidePlank)
        val highSidePlankBtn: Button = findViewById(R.id.highSidePlank)
        val birdDogBtn: Button = findViewById(R.id.birdDog)

        setButtonClickListener(highPlankBtn, "High Plank")
        setButtonClickListener(lowSidePlankBtn, "Low Side Plank")
        setButtonClickListener(highSidePlankBtn, "High Side Plank")
        setButtonClickListener(birdDogBtn, "Bird Dog")
    }

    fun setButtonClickListener(button: Button, title: String) {
        button.setOnClickListener {
            toCameraPage(title)
        }
    }
    fun toCameraPage(title: String){
        val intent = Intent(this , CameraPage::class.java)
        intent.putExtra("TITLE" , title)
        startActivity(intent)
    }
}