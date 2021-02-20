package com.example.tuchatrcsmessenger.Extensions;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.example.tuchatrcsmessenger.R;

public class CustomProgress {
    public static CustomProgress customProgress = null;
    private Dialog mDialog;
    private ProgressBar mProgressBar;

    public static CustomProgress getInstance() {
        if (customProgress == null) {
            customProgress = new CustomProgress();
        }
        return customProgress;
    }

    public void showProgress(Context context, String message, boolean cancelable) {
        mDialog = new Dialog(context);
        // no tile for the dialog
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.custom_progress_bar_layout);
        mProgressBar = (ProgressBar) mDialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        // you can change or add this line according to your need
        mProgressBar.setIndeterminate(true);
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        mDialog.show();
    }

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}