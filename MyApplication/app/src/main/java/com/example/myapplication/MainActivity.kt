package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main)

        // Initialization of Buttons found in activity_main
        val highPlankBtn: Button = findViewById(R.id.highPlank)
        val lowSidePlankBtn: Button = findViewById(R.id.lowSidePlank)
        val highSidePlankBtn: Button = findViewById(R.id.highSidePlank)
        val birdDogBtn: Button = findViewById(R.id.birdDog)

        // Setting onClick Listener and specific functions
        setButtonClickListener(highPlankBtn, "High Plank")
        setButtonClickListener(lowSidePlankBtn, "Low Side Plank")
        setButtonClickListener(highSidePlankBtn, "High Side Plank")
        setButtonClickListener(birdDogBtn, "Bird Dog")
    }

    fun setButtonClickListener(button: Button, title: String) {
        button.setOnClickListener {
            toLevelPage(title)
        }
    }
    fun toLevelPage(title: String){
        val intent = Intent(this , LevelPage::class.java)
        intent.putExtra("TITLE" , title)
        startActivity(intent)
    }
}