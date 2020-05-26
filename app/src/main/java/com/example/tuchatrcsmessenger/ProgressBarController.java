package com.example.tuchatrcsmessenger;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ProgressBarController {
    void showProgressBar(Button signInButton, LinearLayout progressBarLayout){
        //Show progress bar
        signInButton.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    void hideProgressbar(Button signInButton, LinearLayout progressBarLayout){
        //Hide progress bar
        signInButton.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
    }
}
