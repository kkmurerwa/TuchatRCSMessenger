package com.example.tuchatrcsmessenger;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuchatrcsmessenger.Adapters.MessagesAdapter;
import com.example.tuchatrcsmessenger.Classes.ChatroomClass;
import com.example.tuchatrcsmessenger.Classes.messagesClass;
import com.example.tuchatrcsmessenger.data.db.AppDatabase;
import com.example.tuchatrcsmessenger.data.entity.LastMessage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatsActivity extends AppCompatActivity {
    //new chatRoomId to avoid interfering with the other since this is static.
    public static String chatRoomId = null;
    final String userInfoCollection = "users";
    CollectionReference dbPath;
    //Variable to hold messages
    List<messagesClass> messagesList = new ArrayList<>();
    Date date;
    String message;
    boolean mIsNewConversation;
    //Variables for passed intent Extras
    private String phoneNumber;
    private String senderName;
    private String myName;
    private String chatRoomID;
    //Layout variables
    private RecyclerView recyclerView;
    private MessagesAdapter adapter;
    private LinearLayout emptyPlaceholder;
    private EditText typedMessage;
    //Firestore variables for the data path
    private String chatRoomCollection = "chatrooms";
    private String messagesCollection = "messages";
    private String userID;
    private String receiverUserID;
    private int messageCount;
    //Firestore variables to retrieve messages
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    Boolean firstOpened = true;

    private ProgressBar currentProgress = null;
    private AppDatabase mDb;
    private int mUnreadCount;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        hideSoftKeyBoard();

        Toolbar toolbar = findViewById(R.id.chats_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        db = FirebaseFirestore.getInstance();
        //Retrieve the passed strings from calling activity
        Intent intent = getIntent();

        if (intent.hasExtra("Phone Number")) {
            phoneNumber = intent.getStringExtra("Phone Number");
            retrieveSenderUserName();
        }


        if (intent.hasExtra("Sender Name")) {
            senderName = intent.getStringExtra("Sender Name");
            setTitle(senderName);
        }
        if (intent.hasExtra("id")) {
            receiverUserID = intent.getStringExtra("id");
        }

        chatRoomID = intent.getStringExtra("Chat ID");
        chatRoomId = intent.getStringExtra("Chat ID");

        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        userID = firebaseUser.getUid();


        //Initialize the layout variables
        recyclerView = findViewById(R.id.chats_recycler_view);
        emptyPlaceholder = findViewById(R.id.chats_empty_placeholder);

        //Initialize Firestore variable


        //FireStore file structure
        dbPath = db.collection(chatRoomCollection)
                .document(chatRoomID)
                .collection(messagesCollection);


        ImageButton sendMessage = findViewById(R.id.send_message_button);
        typedMessage = findViewById(R.id.typed_message);

        mDb = AppDatabase.getInstance(ChatsActivity.this);

        if (mDb.getLastMessageDao().getLastMessage(chatRoomId) != null){
            mUnreadCount = mDb.getLastMessageDao().getLastMessage(chatRoomId).getUnreadCount();
        }



        //Set button onClickListener
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = typedMessage.getText().toString().trim();

                if (message.length() > 0) {
                    saveMessagesToFirestore();
                    typedMessage.setText("");

                    if (mUnreadCount>0){
                        saveLastMessage(messagesList.get(messagesList.size()-1));
                        adapter.notifyDataSetChanged();
                    }

                    if (messageCount == 0) {
                        createConversation();
                    }
                }
            }
        });

        setAdapter();
        updatesListener();
        retrieveMyUserName();
    }

    public void setCurrentProgress(ProgressBar progress) {
        currentProgress = progress;
    }

    private void hideSoftKeyBoard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    public int getUnreadCount() {
        if (mDb.getLastMessageDao().getLastMessage(chatRoomId) != null){
            return mDb.getLastMessageDao().getLastMessage(chatRoomId).getUnreadCount();
        }
        return 0;
    }

    private void updatesListener() {
        dbPath.orderBy("sentTime")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        messageCount = queryDocumentSnapshots.size();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            messagesList.clear();

                            for (DocumentSnapshot d : list) {
                                messagesClass p = d.toObject(messagesClass.class);

                                messagesClass listItem = new messagesClass(
                                        p.getSenderName(),
                                        p.getMessageBody(),
                                        p.getSentTime(),
                                        d.getId(), p.getUserId()
                                );
                                messagesList.add(listItem);
                            }

                            recyclerView.setVisibility(View.VISIBLE);
                            emptyPlaceholder.setVisibility(View.GONE);

                            adapter.setList(messagesList);
                            adapter.notifyDataSetChanged();

                            if (firstOpened) {
                                firstOpened = false;
                            } else {
                                adapter.setCurrentPosition(adapter.getItemCount() - 1);
                            }
                            recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            emptyPlaceholder.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void saveLastMessage(messagesClass messagesClass) {

        SimpleDateFormat formatterFullDate = new SimpleDateFormat("d MMM yyyy");

        final LastMessage lastMessage = new LastMessage(
                messagesClass.getMessageBody(),
                formatterFullDate.format(messagesClass.getSentTime()),
                chatRoomID,
                0);

        new Thread(new Runnable() {
            @Override
            public void run() {


                mDb.getLastMessageDao().insertLastMessage(lastMessage);

            }
        }).start();
    }


    private void setAdapter() {
        adapter = new MessagesAdapter(ChatsActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);

    }

    private void saveMessagesToFirestore() {
        date = new Date();
        date.getTime();

        String chtRmId = chatRoomID;

        final messagesClass messagesClass = new messagesClass(
                myName,
                message,
                date,
                chtRmId,
                FirebaseAuth.getInstance().getUid()
        );
        dbPath.add(messagesClass).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if (currentProgress != null) {
                    currentProgress.setVisibility(View.GONE);
                    currentProgress = null;
                    adapter.setCurrentPosition(null);
                }
            }
        });

    }

    public void retrieveSenderUserName() {
        db.collection(userInfoCollection)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d : list) {
                                if (Objects.equals(d.getString("user_phone"), phoneNumber)) {
                                    senderName = d.getString("user_name");
                                    receiverUserID = d.getString("user_id");
                                    //Set the name of the sender as action bar title
                                    setTitle(senderName);
                                }
                            }
                        }
                    }
                });


    }

    public void retrieveMyUserName() {
        db.collection(userInfoCollection)
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        myName = documentSnapshot.getString("user_name");
                    }
                });
    }

    public void checkIfConversationExists() {
        db.collection(userInfoCollection)
                .document(userID)
                .collection(chatRoomCollection)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d : list) {
                                if (d.getString("").equals(receiverUserID)) {
                                    //Do stuff
                                }
                            }
                        }
                    }
                });
    }

    public void createConversation() {
        //Get current time and date
        date = new Date();
        date.getTime();
        String rdstatus = "read";
        String chtRmId = chatRoomID;


        messagesClass messagesClass = new messagesClass(
                senderName,
                message,
                date,
                chtRmId, FirebaseAuth.getInstance().getUid()
        );

        messagesClass messagesClassReceiver = new messagesClass(
                myName,
                message,
                date,
                chtRmId, FirebaseAuth.getInstance().getUid()
        );

        List<String> conversationOwners = new ArrayList<>();

        conversationOwners.add(userID);
        conversationOwners.add(receiverUserID);

        ChatroomClass chatroomMembers = new ChatroomClass(conversationOwners);

        //Save user IDs of conversation owners on the conversation
        db.collection(chatRoomCollection)
                .document(chtRmId).set(chatroomMembers);

        db.collection(userInfoCollection)
                .document(userID)
                .collection(chatRoomCollection)
                .document(chtRmId)
                .set(messagesClass);

        db.collection(userInfoCollection)
                .document(receiverUserID)
                .collection(chatRoomCollection)
                .document(chtRmId)
                .set(messagesClassReceiver);
    }

    public String returnMyName() {
        return myName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Creates the menu inflater
        getMenuInflater().inflate(R.menu.chat_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handles clicks on clicked menu items
        int id = item.getItemId();

        if (id == R.id.delete_conversations) {
            //Code to delete conversation

            //Show snackbar with message
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "This feature has not been implemented yet", Snackbar.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLastMessage(messagesList.get(messagesList.size() - 1));
        chatRoomId = null;


        Log.d("ChatsActivityLife", "OnPause Called - ID = " + chatRoomId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatRoomId = chatRoomID;
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        int notifId = sharedpreferences.getInt(chatRoomID, 0);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(notifId);
        }
        Log.d("ChatsActivityLife", "OnResume Called - ID = " + chatRoomId);
    }

    @Override
    public void finish() {
        //This method specifies the actions to do if the user presses back button while in this activity
        super.finish();
        chatRoomId = null;
        //Animate transition to calling activity
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        chatRoomId = null;

        if (getIntent().hasExtra("open_main_activity")) {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }
    }

}