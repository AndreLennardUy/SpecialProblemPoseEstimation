package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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


        val title = intent.getStringExtra("TITLE");
        val difficulty = intent.getStringExtra("LEVEL");
        val score = intent.getDoubleExtra("SCORE" , 0.0)


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


}