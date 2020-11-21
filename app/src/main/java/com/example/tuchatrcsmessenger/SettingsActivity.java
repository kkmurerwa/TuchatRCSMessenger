package com.example.tuchatrcsmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tuchatrcsmessenger.External.CustomProgress;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private TextInputEditText mEditTextName;
    private TextInputEditText mEditTextPhone;

    private TextInputLayout mTextFieldName;
    private TextInputLayout mTextFieldPhone;

    private Button mButtonSaveUserName;
    private Button mButtonSaveUserPhone;

    //Firebase path variables
    final String userCollection = "users";

    private String mUserId;
    private FirebaseFirestore mDb;
    private ProgressDialog mProgressDialog;
    private CustomProgress mCustomProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading user info");
        mProgressDialog.show();

//        mCustomProgress = CustomProgress.getInstance();
//        mCustomProgress.showProgress(this, "Loading stuff", false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Initialize firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserId = mFirebaseAuth.getUid();

        // Initialize text input edit texts
        mEditTextName = findViewById(R.id.text_input_user_name);
        mEditTextPhone = findViewById(R.id.text_input_user_phone);

        // Initialize text fields
        mTextFieldName = findViewById(R.id.text_field_user_name);
        mTextFieldPhone = findViewById(R.id.text_field_user_phone);

        // Initialize save buttons
        mButtonSaveUserName = findViewById(R.id.button_save_user_name);
        mButtonSaveUserPhone = findViewById(R.id.button_save_user_phone);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Do stuff
        retrieveUserName();
    }

    private void retrieveUserName(){
        //Initialize Firebase Firestore Instance
        mDb = FirebaseFirestore.getInstance();

        mDb.collection(userCollection)
                .document(mUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userName = documentSnapshot.getString("user_name");
                        String userPhone = documentSnapshot.getString("user_phone");
                        mEditTextName.setText(userName);
                        mEditTextPhone.setText(userPhone);
                        mProgressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Could not retrieve user info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void buttonClickManager(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sign_out_button:
                //Show Snackbar with message
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Signing out...", Snackbar.LENGTH_LONG).show();

                //Code to log out the user
                mFirebaseAuth.signOut();

                //Check if user exists
                mFirebaseUser = mFirebaseAuth.getCurrentUser();

                final Context context = SettingsActivity.this;

                if (mFirebaseUser == null) {//Exit activity if user was logged out successfully
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
//            case R.id.text_input_user_name:
//                mEditTextName.setFocusable(true);
//                mEditTextName.requestFocus();
//                mButtonSaveUserName.setVisibility(View.VISIBLE);
//                break;
//            case R.id.text_input_user_phone:
//                mEditTextPhone.setFocusable(true);
//                mButtonSaveUserPhone.setVisibility(View.VISIBLE);
//                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        //This method specifies the actions to do if the user presses back button while in this activity
        super.finish();

        //Animate transition to calling activity
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}