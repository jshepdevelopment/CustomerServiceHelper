package com.jshepdevelopment.customerservicehelper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import com.google.android.gms.*;
import android.media.MediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

public class MainActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false; // set to true when you're in the middle of the
    // sign in flow, to know you should not attempt
    // to connect in onStart()
    private GoogleApiClient mGoogleApiClient;


    TextView timerTextView;
    long startTime = 0;
    long newsStartTime = 0;

    public int customerCount = 0;
    public int hangupCount = 0;
    public int helpCount = 0;
    public int correctCount = 0;
    public int holdCount = 0;

    public Boolean mustHelp = false;

    private MediaPlayer correctSound = null;
    private MediaPlayer incorrectSound = null;

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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        correctSound = MediaPlayer.create(this, R.raw.correct);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

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

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Call a Play Games services API method, for example:
            Games.Leaderboards.submitScore(mGoogleApiClient, "CgkI59LTldMSEAIQAQ", customerCount);
        } else {
            // Alternative implementation (or warn user that they must
            // sign in to use this feature)
        }

        servedText.setText(customerCount + " customers served.");
        correctText.setText(correctCount + " correct actions.");

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void helpButton(View view) {

        Animation fadeButton = AnimationUtils.loadAnimation(this, R.anim.fadein);
        ImageButton helpButton = (ImageButton) this.findViewById(R.id.helpButton);
        ImageView resultView = (ImageView) findViewById(R.id.checkresult);
        Animation resultAnimation = AnimationUtils.loadAnimation(this, R.anim.checkresult);


        helpButton.startAnimation(fadeButton);

        // Update correct count
        if(mustHelp) {
            correctSound.start();
            resultView.setImageResource(R.drawable.correct);
            correctCount += 1;
        } else {
            resultView.setImageResource(R.drawable.incorrect);
            incorrectSound.start();
            correctCount -= 1;
        }

        resultView.startAnimation(resultAnimation);

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
        ImageView resultView = (ImageView) findViewById(R.id.checkresult);
        Animation resultAnimation = AnimationUtils.loadAnimation(this, R.anim.checkresult);

        hangupButton.startAnimation(fadeButton);

        // Update correct count
        if(mustHelp) {
            correctCount -= 1;
            resultView.setImageResource(R.drawable.incorrect);
            incorrectSound.start();
        } else {
            resultView.setImageResource(R.drawable.correct);
            correctCount += 1;
            correctSound.start();
        }

        hangupCount += 1;
        nextCustomer();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.sign_out_button) {
            // user explicitly signed out, so turn off auto sign in
            mExplicitSignOut = true;
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
            }
        }

        if (view.getId() == R.id.sign_in_button) {
            // start the asynchronous sign in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
        else if (view.getId() == R.id.sign_out_button) {
            // sign out.
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);

            // show sign-in button, hide the sign-out button
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // show sign-out button, hide the sign-in button
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        // (your code here: update UI, enable functionality that depends on sign in, etc)
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "Airror")) {
                mResolvingConnectionFailure = false;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_other_error);
            }
        }
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

    // Call when the sign-in button is clicked
    public void signInClicked(View view) {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    // Call when the sign-out button is clicked
    public void signOutclicked(View view) {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
    }
}
