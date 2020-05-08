package com.example.tuchatrcsmessenger;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.hbb20.CountryCodePicker;

public class SignInActivity extends AppCompatActivity {
    CountryCodePicker countrySpinner;
    EditText phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Hide action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Initialize phone number EditText
        phoneNumber = findViewById(R.id.phone_entry);


        //Set spinner properties
        countrySpinner = findViewById(R.id.country_spinner);


        //Set onclick listener for sign in
        final Button signInButton = findViewById(R.id.sign_in_button);


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
                String fullPhoneNumber = countrySpinner.getFullNumber();

                if (countrySpinner.isValidFullNumber()) {
                    Toast.makeText(SignInActivity.this, "Phone number is valid", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SignInActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
//                    LinearLayout progressBarLayout = findViewById(R.id.progress_circular);
//                    signInButton.setVisibility(View.GONE);
//                    progressBarLayout.setVisibility(View.VISIBLE);
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
}
