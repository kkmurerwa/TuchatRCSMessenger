package com.example.tuchatrcsmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void buttonClickManager(View v){
        int id = v.getId();
        switch (id){
            case R.id.sign_out_button:
                //Show Snackbar with message
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Signing out...", Snackbar.LENGTH_LONG).show();

                //Code to log out the user
                mFirebaseAuth.signOut();

                //Check if user exists
                mFirebaseUser = mFirebaseAuth.getCurrentUser();

                final Context context = SettingsActivity.this;

                if (mFirebaseUser == null){//Exit activity if user was logged out successfully
                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            //Add code after every second
                        }

                        public void onFinish() {
                            //Finish current activity
                            SettingsActivity.this.finish();

                            //Launch welcome screen activity
                            startActivity(new Intent(SettingsActivity.this, WelcomeScreen.class));
                        }
                    }.start();
                } else {
                    Snackbar.make(parentLayout, "Could not sign you out", Snackbar.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

}