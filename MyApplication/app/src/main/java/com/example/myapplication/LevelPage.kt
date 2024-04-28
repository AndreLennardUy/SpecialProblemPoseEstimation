package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LevelPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_page)

        // back to main view
        val backBtn: ImageButton = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        //Init Buttons
        var easyBtn : Button = findViewById(R.id.easy)
        easyBtn.setOnClickListener{
            levelSelection(1);
        }
        var intBtn : Button = findViewById(R.id.intermediate)
        intBtn.setOnClickListener {
            levelSelection(2);
        }
        var expBtn : Button = findViewById(R.id.expert)
        expBtn.setOnClickListener {
            levelSelection(3);
        }

        val title = intent.getStringExtra("TITLE") ?: "Title"
        val titleTextView: TextView = findViewById(R.id.title)
        titleTextView.text = title
    }

    fun levelSelection(selected : Int){
        when(selected){
            1 -> toCameraPage("Easy");
            2 -> toCameraPage("Intermediate");
            3 -> toCameraPage("Expert");
        }
    }

    fun toCameraPage(level: String) {
        val title = intent.getStringExtra("TITLE")
        val intent = Intent(this, CameraPage::class.java)
        intent.putExtra("LEVEL", level)
        title?.let {
            intent.putExtra("TITLE", it)
        }
        startActivity(intent)
    }

}