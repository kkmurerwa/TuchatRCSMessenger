package com.example.tuchatrcsmessenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tuchatrcsmessenger.Controllers.ProgressBarController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//TODO: Fix bug where this activity is skipped if user stops the process mid-process and then returns later
//TODO: Add a progress monitor via shared preferences that will keep track of log in progress and fix above bug

public class ScreenNameSelectionActivity extends AppCompatActivity {
    //Firebase path variables
    final String userCollection = "users";
    //Firebase Firestore Variables
    FirebaseFirestore db;
    //Firebase variables
    FirebaseAuth firebaseAuth;
    String userID;
    String userPhoneNumber;
    private Button continueButton;
    private EditText displayName;
    private LinearLayout screenNameProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_name_selection);

        //Get phone number from previous activity through added intent extra
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                userPhoneNumber = null;
            } else {
                userPhoneNumber = extras.getString("Phone Number");
            }
        } else {
            userPhoneNumber = (String) savedInstanceState.getSerializable("Phone Number");
        }

        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        //Initialize View elements
        displayName = findViewById(R.id.screen_name_entry);
        continueButton = findViewById(R.id.screen_name_button);
        screenNameProgressBar = findViewById(R.id.screen_name_progress_bar);

        //Set on-click listener for button
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Code to save display name to firebase
                String enteredDisplayName = displayName.getText().toString().trim();
                if (displayName.length() >= 3) {
                    //Do some stuff first
                    //Show progress bar
                    ProgressBarController progressBarController = new ProgressBarController();
                    progressBarController.showProgressBar(continueButton, screenNameProgressBar);

                    saveToFireStore(ScreenNameSelectionActivity.this, enteredDisplayName);
                } else {
                    Toast.makeText(ScreenNameSelectionActivity.this, "Display name should be at least" +
                            " three characters long.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void saveToFireStore(final Context context, String displayName) {
        //Initialize Firebase Firestore Instance
        db = FirebaseFirestore.getInstance();

        // Create a new user Array and append the display name to it
        Map<String, Object> userArray = new HashMap<>();
        userArray.put("user_name", displayName);
        userArray.put("user_phone", userPhoneNumber);
        userArray.put("user_id", userID);

        //Create user collection if it does not exist and save information
        db.collection(userCollection).document(userID)
                .set(userArray, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Call main activity
                        Intent mainActivity = new Intent(ScreenNameSelectionActivity.this, MainActivity.class);
                        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainActivity);

                        //Animate transition into called activity
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Display error message
                        Toast.makeText(context, "Failed to save display name. Please try again.", Toast.LENGTH_LONG).show();

                        //Hide progress bar
                        ProgressBarController progressBarController = new ProgressBarController();
                        progressBarController.hideProgressbar(continueButton, screenNameProgressBar);
                    }
                });
    }

    //This method specifies the actions to do if the user presses back button while in this activity
    @Override
    public void finish() {
        super.finish();

        //Animate transition to calling activity
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
