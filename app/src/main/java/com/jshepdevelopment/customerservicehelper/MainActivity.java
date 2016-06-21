package com.jshepdevelopment.customerservicehelper;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Handler;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity {

    TextView timerTextView;
    long startTime = 0;
    long newsStartTime = 0;

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

    Handler newsTimerHandler = new Handler();
    Runnable newsTimerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - newsStartTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            Log.d("JSLOG", "Newsfeed seconds: " + seconds);

            // Update news item every 15 seconds
            if(seconds % 15 == 0) {
                nextNewsItem();
            }
            newsTimerHandler.postDelayed(this, 500);
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

        TextView newsfeedTextView=(TextView) findViewById(R.id.newsfeedmarquee);
        newsfeedTextView.setSelected(true);

        //Start the next customer animation
        nextCustomer();

        newsStartTime = System.currentTimeMillis();
        newsTimerHandler.postDelayed(newsTimerRunnable, 0);

    }

    public void nextNewsItem() {

        Random rand = new Random();
        int randomNum;

        // Gets text from array
        TypedArray newsFeedItems = getResources().obtainTypedArray(R.array.newsitems);
        randomNum = rand.nextInt(newsFeedItems.length());

        // Assign textview based on random value from array
        TextView newsfeedTextView=(TextView) findViewById(R.id.newsfeedmarquee);
        newsfeedTextView.setSelected(true);
        newsfeedTextView.setText(newsFeedItems.getText(randomNum));

        Log.d("JSLOG", "Newsfeed item updated.");
    }

    public void nextCustomer() {

        // Setup random numbers for customer type
        Random rand = new Random();
        int randomNum;

        // Setup  TypedArrays for good and bad images
        TypedArray goodImages = getResources().obtainTypedArray(R.array.good_images);
        TypedArray badImages = getResources().obtainTypedArray(R.array.bad_images);

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

        Animation fadeButton = AnimationUtils.loadAnimation(this, R.anim.fadein);
        ImageButton helpButton = (ImageButton) this.findViewById(R.id.helpButton);

        helpButton.startAnimation(fadeButton);

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
        Animation fadeButton = AnimationUtils.loadAnimation(this, R.anim.fadein);
        ImageButton holdButton = (ImageButton) this.findViewById(R.id.holdButton);

        holdButton.startAnimation(fadeButton);
        nextCustomer();
    }

    public void hangupButton(View view) {
        Animation fadeButton = AnimationUtils.loadAnimation(this, R.anim.fadein);
        ImageButton hangupButton = (ImageButton) this.findViewById(R.id.hangupButton);

        hangupButton.startAnimation(fadeButton);

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

    @Override
    public void onResume() {
        super.onResume();
        nextCustomer();
    }
}
