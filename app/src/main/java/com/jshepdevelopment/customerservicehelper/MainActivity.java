package com.jshepdevelopment.customerservicehelper;

import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up customer View
        ImageView customerView = (ImageView) findViewById(R.id.customer);

        //Set up animation
        Animation customerMove = AnimationUtils.loadAnimation(this, R.anim.move_customer);
        //Start the animation
        customerView.startAnimation(customerMove);
    }

    public void nextCustomer(View view) {

        // Setup random numbers for customer type
        Random rand = new Random();
        int randomNum;

        // Setup  TypedArrays for good and bad images
        TypedArray goodImages = getResources().obtainTypedArray(R.array.good_images);
        TypedArray badImages = getResources().obtainTypedArray(R.array.bad_images);

        Log.d("JSLOG", goodImages.length() + " good images.");
        Log.d("JSLOG", badImages.length() + " bad images.");

        Customer nextCustomer = new Customer();
        ImageView customerView = (ImageView) findViewById(R.id.customer);
        Animation customerMove = AnimationUtils.loadAnimation(this, R.anim.move_customer);

        // Set next customer type
        nextCustomer.setCustomerType();
        Log.d("JSLOG", "newCustomer.isGood = " + nextCustomer.isGood);

        // Set customer image view based on customer type
        if(nextCustomer.isGood) {
            randomNum = rand.nextInt(goodImages.length());
            Log.d("JSLOG", "good randomNum is " + randomNum);
            customerView.setImageDrawable(goodImages.getDrawable(randomNum));


        } else {
            randomNum = rand.nextInt(badImages.length());
            Log.d("JSLOG", "bad randomNum is " + randomNum);
            customerView.setImageDrawable(badImages.getDrawable(randomNum));

        }

        // Start the animation
        customerView.startAnimation(customerMove);

    }
}
