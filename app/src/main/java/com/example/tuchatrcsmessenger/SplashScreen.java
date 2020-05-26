package com.example.tuchatrcsmessenger;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    //Global variable
    Intent welcomeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Hide action bar
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).hide();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        new CountDownTimer(1500, 1000) {

            public void onTick(long millisUntilFinished) {
                //Add code after every second
            }

            public void onFinish() {
                //Perform actions when timer is finished
                if (firebaseUser != null){
                    //Add code to do if user is logged in
                    Intent mainActivity = new Intent(SplashScreen.this, MainActivity.class);

                    //Finish current activity
                    finish();

                    //Start next activity
                    startActivity(mainActivity);

                    //Animate transition into called activity
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                else {
                    //Add code here if user is not logged in
                    welcomeScreen = new Intent(SplashScreen.this, WelcomeScreen.class);

                    //Finish current activity
                    finish();

                    //Call new activity
                    startActivity(welcomeScreen);

                    //Animate transition into called activity
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        }.start();
    }
}
