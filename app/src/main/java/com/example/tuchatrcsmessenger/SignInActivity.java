package com.example.tuchatrcsmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tuchatrcsmessenger.Classes.SaveTokenObject;
import com.example.tuchatrcsmessenger.Controllers.ProgressBarController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {
    CountryCodePicker countrySpinner;
    EditText phoneNumber;
    String fullPhoneNumber;
    Button signInButton;
    TextView phoneNumberPrompt, phoneNumberDisclaimer;
    LinearLayout progressBarLayout;
    Boolean verificationMode = false;

    //Firebase variables
    FirebaseAuth firebaseAuth;
    String verificationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        //Firebase Auth initialization
        firebaseAuth = FirebaseAuth.getInstance();

        //Initialize phone number EditText
        phoneNumber = findViewById(R.id.phone_entry);

        //Initialize the TextViews
        phoneNumberPrompt = findViewById(R.id.phone_number_prompt);
        phoneNumberDisclaimer = findViewById(R.id.phone_number_disclaimer);

        //Initialize the progress bar container
        progressBarLayout = findViewById(R.id.progress_circular);


        //Set spinner properties
        countrySpinner = findViewById(R.id.country_spinner);


        //Set onclick listener for sign in
        signInButton = findViewById(R.id.sign_in_button);


        //Attach EditText to cpp
        countrySpinner.registerCarrierNumberEditText(phoneNumber);
        countrySpinner.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                // your code
            }
        });


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userVerificationCode;

                //Check if the user is verifying the code received or asking for one
                if (!verificationMode) {
                    //Get full phone number from the phone code library
                    fullPhoneNumber = countrySpinner.getFullNumberWithPlus();

                    if (countrySpinner.isValidFullNumber()) {
                        //Call sign-in method
                        signInMethod(fullPhoneNumber);

                        //Show progress bar
                        ProgressBarController progressBarController = new ProgressBarController();
                        progressBarController.showProgressBar(signInButton, progressBarLayout);
                    } else {
                        Toast.makeText(SignInActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Get the user-inputted verification code
                    userVerificationCode = phoneNumber.getText().toString().trim();
                    if (!userVerificationCode.equals("")) {
                        verifyCode(userVerificationCode);

                        //Show progress bar
                        ProgressBarController progressBarController = new ProgressBarController();
                        progressBarController.showProgressBar(signInButton, progressBarLayout);
                        TextView textView = findViewById(R.id.progress_bar_text);
                        textView.setText("Please wait...");
                    }
                }
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

    public void signInMethod(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            //Detect the code automatically
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                //Call verify code method if code is automatically retrieved
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Log.e("LogInError", e.getLocalizedMessage() + e.getMessage());
            ProgressBarController controller = new ProgressBarController();
            controller.hideProgressbar(signInButton, progressBarLayout);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationID = s;
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            //Do something if auto retrieval times out
            super.onCodeAutoRetrievalTimeOut(s);

            //Change to verification mode only if the number is valid
            verificationMode = true;
            changeToVerificationMode();
        }
    };


    private void verifyCode(String code) {
        //This verifies the code either entered by the user or auto-retrieved
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, code);
        signInWithCredential(phoneAuthCredential);
    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            //Save Token to Firestore
                            saveToken();

                        } else {
                            //Hide progress bar
                            ProgressBarController controller = new ProgressBarController();
                            controller.hideProgressbar(signInButton, progressBarLayout);
                        }
                    }
                });
    }

    private void saveToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String deviceToken = instanceIdResult.getToken();

                        sendRegistrationToServer(deviceToken);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Token", "NewTokenFailed " + e.getLocalizedMessage());
            }
        });
    }

    private void sendRegistrationToServer(String deviceToken) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d(
                    "MessagingService",
                    "sendRegistrationToServer: sending token to server:  " + deviceToken
            );

            SaveTokenObject saveTokenObject = new SaveTokenObject(deviceToken);

            FirebaseFirestore.getInstance().collection("tokens").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(saveTokenObject)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Hide progress bar
                            ProgressBarController progressBarController = new ProgressBarController();
                            progressBarController.hideProgressbar(signInButton, progressBarLayout);
                            Log.d("Token", "NewTokenFailedSend " + e.getLocalizedMessage());

                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Hide progress bar
                    ProgressBarController progressBarController = new ProgressBarController();
                    progressBarController.hideProgressbar(signInButton, progressBarLayout);
                    //Call next activity
                    Intent screenNameSelection = new Intent(SignInActivity.this, ScreenNameSelectionActivity.class);
                    screenNameSelection.putExtra("Phone Number", fullPhoneNumber);
                    startActivity(screenNameSelection);

                    //Animate transition into called activity
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

        } else {
            //Hide progress bar
            ProgressBarController progressBarController = new ProgressBarController();
            progressBarController.hideProgressbar(signInButton, progressBarLayout);
            Log.d("Token", "NewTokenFailedSend User Null");
        }
    }

    private void changeToVerificationMode() {
        //Modify phone number prompt
        phoneNumberPrompt.setText("Enter verification Code");

        //Modify phone number disclaimer
        phoneNumberDisclaimer.setText(R.string.manual_code_entry_disclaimer);

        //Hide the country code spinner
        countrySpinner.setVisibility(View.GONE);

        //Change hint on phone number EditText to "Enter code"
        phoneNumber.setText("");
        phoneNumber.setHint("Enter code");
        countrySpinner.setNumberAutoFormattingEnabled(false);

        //Change button text to "Submit code"
        signInButton.setText("Submit code");

        //Hide progress bar
        ProgressBarController progressBarController = new ProgressBarController();
        progressBarController.hideProgressbar(signInButton, progressBarLayout);
    }

}
