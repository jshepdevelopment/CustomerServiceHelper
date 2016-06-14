package com.jshepdevelopment.customerservicehelper;

import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.concurrent.RunnableFuture;

public class MainActivity extends AppCompatActivity {

    TextView timerTextView;
    long startTime = 0;
    public int customerCount = 0;
    public int hangupCount = 0;
    public int helpCount = 0;
    public int correctCount = 0;
    public int holdCount = 0;

    public Boolean mustHelp = false;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText("Call time is " + String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up customer View
        //ImageView customerView = (ImageView) findViewById(R.id.customer);
        timerTextView = (TextView) findViewById(R.id.timerText);

        // Set up text view
        TextView textView = (TextView) findViewById(R.id.servedText);
        textView.setText(customerCount + " customers served.");

        //Set up animation
        //Animation customerMove = AnimationUtils.loadAnimation(this, R.anim.move_customer);
        //Start the animation
        //customerView.startAnimation(customerMove);
        nextCustomer();
    }

    public void nextCustomer() {

        // Setup random numbers for customer type
        Random rand = new Random();
        int randomNum;

        // Setup  TypedArrays for good and bad images
        TypedArray goodImages = getResources().obtainTypedArray(R.array.good_images);
        TypedArray badImages = getResources().obtainTypedArray(R.array.bad_images);

        Log.d("JSLOG", goodImages.length() + " good images.");
        Log.d("JSLOG", badImages.length() + " bad images.");

        Customer nextCustomer = new Customer();
        TextView servedText = (TextView) findViewById(R.id.servedText);
        TextView correctText = (TextView) findViewById(R.id.correctText);
        ImageView customerView = (ImageView) findViewById(R.id.customer);
        Animation customerMove = AnimationUtils.loadAnimation(this, R.anim.move_customer);

        // Set next customer type
        nextCustomer.setCustomerType();
        Log.d("JSLOG", "newCustomer.isGood = " + nextCustomer.isGood);

        // Set customer image view based on customer type
        if(nextCustomer.isGood) {
            mustHelp = true;
            randomNum = rand.nextInt(goodImages.length());
            Log.d("JSLOG", "good randomNum is " + randomNum);
            customerView.setImageDrawable(goodImages.getDrawable(randomNum));


        } else {
            mustHelp = false;
            randomNum = rand.nextInt(badImages.length());
            Log.d("JSLOG", "bad randomNum is " + randomNum);
            customerView.setImageDrawable(badImages.getDrawable(randomNum));

        }

        // Start the animation
        customerView.startAnimation(customerMove);

        // Increase customer count and update text view
        customerCount+=1;
        servedText.setText(customerCount + " customers served.");
        correctText.setText(correctCount + " correct actions.");

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void helpButton(View view) {

        // Update correct count
        if(mustHelp) {
            correctCount += 1;
        } else {
            correctCount -= 1;
        }

        helpCount += 1;
        nextCustomer();
    }

    public void holdButton(View view) {

        nextCustomer();
    }

    public void hangupButton(View view) {

        // Update correct count
        if(mustHelp) {
            correctCount -= 1;
        } else {
            correctCount += 1;
        }

        hangupCount += 1;
        nextCustomer();
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
