package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ScorePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_page)

        var titleTxt: TextView = findViewById(R.id.pose);
        var difficultyTxt : TextView = findViewById(R.id.level);
        var scoreTxt : TextView = findViewById(R.id.score);
        var finishBtn : Button = findViewById(R.id.finishBtn);
        var display : ImageView = findViewById(R.id.display);


        val title = intent.getStringExtra("TITLE");
        val difficulty = intent.getStringExtra("LEVEL");
        val score = intent.getDoubleExtra("SCORE" , 0.0)

        val bitmap = retrieveBitmapFromTempStorage(intent)
        if (bitmap != null) {
            display.setImageBitmap(bitmap)
        } else {
            // Handle the case when bitmap retrieval fails (e.g., display a placeholder image)
            // You can set a placeholder image or show an error message here
            display.setImageResource(R.drawable.ic_launcher_background)
        }

        titleTxt.text = title;
        difficultyTxt.text = difficulty;
        scoreTxt.text = score.toString();

        // Create AlertDialog.Builder
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Set dialog properties
        alertDialogBuilder
            .setTitle("ATTENTION")
            .setMessage("Before Exiting this page make sure to take a screenshot of your score to be presented to your teacher")
            .setCancelable(false) // Prevent dismissing dialog by tapping outside

            // Positive button
            .setPositiveButton("OK") { dialog, which ->
                val intent = Intent(this , MainActivity::class.java);
                startActivity(intent);
                dialog.dismiss()
            }

            // Negative button
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        finishBtn.setOnClickListener {
            // Create and show the AlertDialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
    fun retrieveBitmapFromTempStorage(intent: Intent): Bitmap? {
        val uriString = intent.getStringExtra("FRAME")
        val uri = Uri.parse(uriString)
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
}