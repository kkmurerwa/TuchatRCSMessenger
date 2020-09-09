package com.example.tuchatrcsmessenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

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

            String sender = remoteMessage.getData().get("sender_name");

            String sender_id = remoteMessage.getData().get("sender_id");

            String chatRoomId = remoteMessage.getData().get("chatRoom_id");

            buildNotification(title, message, sender, sender_id, chatRoomId);

        }
        Log.d("MessagingService", "Received message " + remoteMessage.getData().toString());

    }

    private void buildNotification(String title, String message, String sender, String sender_id, String chatRoomId) {

        Intent intent = new Intent(this, ChatsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.putExtra("Sender Name", sender);
        intent.putExtra("id", sender_id);
        intent.putExtra("Chat ID", chatRoomId);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = 1;
            String channelId = "channel-01";
            String channelName = "Chat Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(sender)
                    .setStyle(
                            new NotificationCompat
                                    .BigTextStyle()
                                    .bigText(message)
                                    .setBigContentTitle(sender)
                                    .setSummaryText("New message")
                    )
                    .setSmallIcon(R.drawable.user_icon)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    getApplicationContext().getResources(),
                                    R.drawable.user_icon
                            )
                    )
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true);

            mBuilder.setContentIntent(notifyPendingIntent);
            assert notificationManager != null;
            notificationManager.notify(notificationId, mBuilder.build());

        } else {
            int notificationId = buildNotificationId(chatRoomId);
            // Instantiate a Builder object.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    this, "default_notification_channel_name"
            );

            //add properties to the builder
            builder.setSmallIcon(R.drawable.user_icon)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    getApplicationContext().getResources(),
                                    R.drawable.user_icon
                            )
                    )
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(sender)
                    // .setContentText(message)
                    .setSubText(message)
                    .setColor(Color.parseColor("#66a3ff"))
                    .setAutoCancel(true)
                    .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText(message)
                    )
                    .setOnlyAlertOnce(true);
            builder.setContentIntent(notifyPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationId, builder.build());
        }


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

    private Integer buildNotificationId(String id) {

        int notificationId = 0;

        for (int i = 0; i <= 8; i++) {
            notificationId += (id.charAt(0));
        }
        return notificationId;
    }
}
