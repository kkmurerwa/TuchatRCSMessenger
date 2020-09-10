package com.example.tuchatrcsmessenger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.tuchatrcsmessenger.Adapters.ConversationsAdapter;
import com.example.tuchatrcsmessenger.Classes.ConversationsClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //Firebase path variables
    final String userInfoCollection = "users";
    final String chatRoomsCollection = "chatrooms";
    final String messagesCollection = "messages";
    //Global variable
    Intent welcomeScreen;
    //Placeholder Layout
    LinearLayout emptyPlaceholder;
    //Progress bar loader
    ProgressBar progressBarLoader;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String userID;
    //RecyclerView
    private RecyclerView conversationsRecyclerView;
    private ConversationsAdapter adapter;
    private List<ConversationsClass> listItems;
    private CollectionReference dbConversationsCollection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.main_toolbar);

        setSupportActionBar(toolbar);


        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        db = FirebaseFirestore.getInstance();


        emptyPlaceholder = findViewById(R.id.empty_placeholder);

        progressBarLoader = findViewById(R.id.conversations_progress_bar);

        assert firebaseUser != null;
        userID = firebaseUser.getUid();

        //Initialize variable for Conversations retrieval
        dbConversationsCollection = db.collection(userInfoCollection)
                .document(userID)
                .collection(chatRoomsCollection);

        //Initialize RecyclerView
        conversationsRecyclerView = findViewById(R.id.conversations_recycler_view);
        conversationsRecyclerView.setHasFixedSize(true);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Declare arrays to be used to hold chat data
        listItems = new ArrayList<>();

        getDataFromFireStore();
        conversationUpdatesListener();

        //Set on-click listener for FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call the new conversations screen
                Intent newConversationActivity = new Intent(MainActivity.this, NewConversationActivity.class);
                startActivity(newConversationActivity);

                //Animate transition into called activity
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


    }

    private void conversationUpdatesListener() {
        dbConversationsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                getDataFromFireStore();
            }
        });
    }

    public void getDataFromFireStore() {
        //Get data from database
        dbConversationsCollection
                .get(Source.CACHE)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();


                            if (!listItems.isEmpty()) {
                                listItems.clear();
                            }

                            for (DocumentSnapshot d : list) {
                                ConversationsClass p = d.toObject(ConversationsClass.class);

                                ConversationsClass listItem = new ConversationsClass(
                                        p.getSenderName(),
                                        p.getMessageBody(),
                                        p.getSentTime(),
                                        d.getId()
                                );
                                listItems.add(listItem);
                            }

//                            adapter = new ConversationsAdapter(listItems, MainActivity.this);
//                            conversationsRecyclerView.setAdapter(adapter);

                            conversationsRecyclerView.setVisibility(View.VISIBLE);
                            progressBarLoader.setVisibility(View.GONE);
                            emptyPlaceholder.setVisibility(View.GONE);
                        } else {
                            //Show empty placeholder layout
                            conversationsRecyclerView.setVisibility(View.GONE);
                            emptyPlaceholder.setVisibility(View.VISIBLE);
                            progressBarLoader.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call the adapter to retrieve last message
        if (!(listItems == null)) {
            adapter = new ConversationsAdapter(listItems, MainActivity.this);
            conversationsRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Creates the menu inflater
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handles clicks on clicked menu items
        int id = item.getItemId();

        if (id == R.id.settings) {
            //Code to log out the user
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return true;
    }

}