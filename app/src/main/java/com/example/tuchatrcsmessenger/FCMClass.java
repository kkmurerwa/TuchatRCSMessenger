package com.example.tuchatrcsmessenger;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tuchatrcsmessenger.Classes.SaveTokenObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Objects;

public class FCMClass extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("MessagingService", "onMessageReceived Called");


        String identifyDataType =
                remoteMessage.getData().get("data_type");

        if (Objects.equals(identifyDataType, "data_type_chat_message")) {
            String title = remoteMessage.getData().get("title");

            String message = remoteMessage.getData().get("message");

            String sender = remoteMessage.getData().get("sender_name ");
        }
        Log.d("MessagingService", "Received message " + remoteMessage.getData().toString());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        saveToken();
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
                            Log.d("Token", "NewTokenFailedSend " + e.getLocalizedMessage());

                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });

        } else {
            Log.d("Token", "NewTokenFailedSend User Null");
        }
    }
}
