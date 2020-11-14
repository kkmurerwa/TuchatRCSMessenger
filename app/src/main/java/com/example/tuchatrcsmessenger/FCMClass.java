package com.example.tuchatrcsmessenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.app.RemoteInput;

import com.example.tuchatrcsmessenger.Classes.SaveTokenObject;
import com.example.tuchatrcsmessenger.External.TinyDB;
import com.example.tuchatrcsmessenger.data.db.AppDatabase;
import com.example.tuchatrcsmessenger.data.entity.LastMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class FCMClass extends FirebaseMessagingService {
    private static final String KEY_TEXT_REPLY = "key_text_reply" ;
    ArrayList<String> mSet = new ArrayList<>();
    private SharedPreferences mSharedpreferences;
    private TinyDB mTinyDB;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        mSharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);

        mTinyDB = new TinyDB(this);

        Log.d("MessagingService", "onMessageReceived Called");
        String identifyDataType = remoteMessage.getData().get("data_type");

        if (Objects.equals(identifyDataType, "data_type_chat_message")) {
            // Save message details
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String sender = remoteMessage.getData().get("sender_name");
            String sender_id = remoteMessage.getData().get("sender_id");
            String chatRoomId = remoteMessage.getData().get("chatRoom_id");
            String sentTime = remoteMessage.getData().get("sentTime");

            char[] chars = sentTime.toCharArray();

            for (int i = chars.length - 1; i >= 0; i--) {
                if (chars[i] == ' ') {
                    sentTime = (sentTime.substring(0, i)).trim();
                    break;
                }
            }

            Log.d("SentTimeF", sentTime);

            SimpleDateFormat formatterFullDate = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzz");
            SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");

            try {
                Date strDate = formatterFullDate.parse(sentTime);

                LastMessage lastMessage = new LastMessage(message, formatter.format(strDate), chatRoomId, 1);

                //Build notification if conversation is not open or app is not open
                if (ChatsActivity.chatRoomId == null) {
                    saveLastMessage(lastMessage, chatRoomId);
                    buildNotification(title, message, sender, sender_id, chatRoomId);
                } else {
                    if (!ChatsActivity.chatRoomId.equals(chatRoomId)) {
                        saveLastMessage(lastMessage, chatRoomId);
                        buildNotification(title, message, sender, sender_id, chatRoomId);
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("SentTimeE", e.getLocalizedMessage());
            }
        }
        Log.d("MessagingService", "Received message " + remoteMessage.getData().toString());
    }

    private void saveLastMessage(LastMessage lastMessage, String chatRoomId) {
        AppDatabase db = AppDatabase.getInstance(this);
        int existingUnreadCount = db.getLastMessageDao().getLastMessage(chatRoomId).getUnreadCount();
        lastMessage.setUnreadCount(existingUnreadCount + 1);
        db.getLastMessageDao().insertLastMessage(lastMessage);
    }


    private void buildNotification(String title, String message, String sender, String sender_id, String chatRoomId) {
        saveNotifMessages(chatRoomId, message);

        // Create intent
        Intent intent = new Intent(this, ChatsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("Sender Name", sender);
        intent.putExtra("id", sender_id);
        intent.putExtra("Chat ID", chatRoomId);
        intent.putExtra("open_main_activity", true);

        // Create list to save multiple messages
        ArrayList<String> unreadMessages = mTinyDB.getListString(chatRoomId);

        Log.d("Set", unreadMessages+" end ");

        // Create and save notification id
        int notificationId = buildNotificationId(chatRoomId);


        saveNotifID(notificationId, chatRoomId);

        // Create pending intent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
        );

        // Create notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "channel-01";
        String channelName = "Chat Channel";

        // Build notification channel for android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }

        }

        // Return unread messages
        int messagesCount = unreadMessages.size();



        // Create notification builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(sender)
                .setContentText(unreadMessages.get(0))
                .setStyle(new NotificationCompat
                                .BigTextStyle()
                                .bigText(unreadMessages.get(0))
                                .setBigContentTitle(sender +": " +message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        BitmapFactory.decodeResource(
                                getApplicationContext().getResources(),
                                R.mipmap.ic_launcher
                        )
                )
                .setOnlyAlertOnce(true)
                .setNumber(messagesCount)
                .setAutoCancel(true);

        // Create inbox-style notifications
        Notification mBuilderInbox;
        if (messagesCount > 3) {
            mBuilderInbox = new NotificationCompat.InboxStyle(mBuilder)
                    .addLine(unreadMessages.get(0))
                    .addLine(unreadMessages.get(1))
                    .addLine(unreadMessages.get(2) +" " +"(+" +(messagesCount-3) +" unread)")
                    .setBigContentTitle(sender)
                    .setSummaryText(messageCounter(messagesCount))
                    .build();
        } else if (messagesCount == 3){
            mBuilderInbox = new NotificationCompat.InboxStyle(mBuilder)
                    .addLine(unreadMessages.get(0))
                    .addLine(unreadMessages.get(1))
                    .addLine(unreadMessages.get(2))
                    .setBigContentTitle(sender)
                    .setSummaryText(messageCounter(messagesCount))
                    .build();
        } else if (messagesCount == 2){
            mBuilderInbox = new NotificationCompat.InboxStyle(mBuilder)
                    .addLine(unreadMessages.get(0))
                    .addLine(unreadMessages.get(1))
                    .setBigContentTitle(sender)
                    .setSummaryText(messageCounter(messagesCount))
                    .build();
        } else {
            mBuilderInbox = new NotificationCompat.InboxStyle(mBuilder)
                    .addLine(unreadMessages.get(0))
                    .setBigContentTitle(sender)
                    .setSummaryText(messageCounter(messagesCount))
                    .build();
        }

        mBuilder.setContentIntent(notifyPendingIntent);

        if (mNotificationManager != null) {
            mNotificationManager.notify(notificationId, mBuilderInbox);
        }
    }

    public String messageCounter(int messageCount) {
        if (messageCount > 1) return messageCount +" new messages";
        else return messageCount +" new message";
    }

    private void saveNotifID(int notificationId, String chatRoomId) {
        SharedPreferences.Editor editor = mSharedpreferences.edit();
        editor.putInt(chatRoomId, notificationId);
        editor.apply();
    }

    private void saveNotifMessages(String chatRoomId, String message){
        // Get from shared prefs using TinyDB
        mSet = mTinyDB.getListString(chatRoomId);

        // Add latest message
        mSet.add(message);

        // Save to shared pref using TinyDB again
        mTinyDB.putListString(chatRoomId, mSet);
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
