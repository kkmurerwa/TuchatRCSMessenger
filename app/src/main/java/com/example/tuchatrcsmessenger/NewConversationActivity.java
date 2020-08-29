package com.example.tuchatrcsmessenger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuchatrcsmessenger.Adapters.ContactsAdapter;
import com.example.tuchatrcsmessenger.Classes.ContactsInfoClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class NewConversationActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    //Firebase path variables
    final String userInfoCollection = "users";

    //Firestore Path Variables
    private String contactsCollection = "contacts";
    private String userID;
    private String myContactsSubcollection = "My Contacts";

    //New Conversations activity variables
    private RecyclerView newConversationRecyclerView;
    private LinearLayout progressBarLayout;
    private LinearLayout placeHolderLayout;

    //Contact permission variables
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private int dismissStatus = 0;

    private List<ContactsInfoClass> contactsListItems;
    private List<ContactsInfoClass> contactsOnTuchatListItem;
    private CollectionReference dbContactsCollection;
    private List<ContactsInfoClass> contactsOnTuchatFromFireStore;
    private ContactsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);

        //Set action bar Title

        Toolbar toolbar = findViewById(R.id.new_chat_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle("Start conversation");

        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        assert firebaseUser != null;
        userID = firebaseUser.getUid();

        //Initialize variable for Conversations retrieval
        dbContactsCollection = db.collection(contactsCollection)
                .document(userID)
                .collection(myContactsSubcollection);


        //Initialize activity elements
        newConversationRecyclerView = findViewById(R.id.new_conversations_recycler_view);
        newConversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newConversationRecyclerView.setHasFixedSize(true);


        progressBarLayout = findViewById(R.id.new_conversations_progress_bar);
        placeHolderLayout = findViewById(R.id.new_conversations_empty_placeholder);

        requestContactsPermission();
        contactsOnTuchatListItem = new ArrayList<>();
        contactsOnTuchatFromFireStore = new ArrayList<>();

        getContactsFromFirestore();
        updatesListener();
    }

    public void requestContactsPermission() {
        int readContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (readContacts != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                //Do something if request was previously denied
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                //Do something if request has never been denied or accepted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int readContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (readContacts != PackageManager.PERMISSION_GRANTED) {
            //Build dialog box
            AlertDialog.Builder builder = new AlertDialog.Builder(NewConversationActivity.this);

            builder.setMessage(R.string.contacts_request_message)
                    .setTitle("Read contacts request").setIcon(R.drawable.user_icon);

            // Add the buttons
            builder.setPositiveButton("That's Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dismissStatus = 1;
                    requestContactsPermission();
                }
            });
            builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dismissStatus = 0;
                }
            });

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (dismissStatus == 0) {
                        Toast.makeText(NewConversationActivity.this, "You cannot proceed without this permission", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();

        } else {
            Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
        }

    }


    public void getContacts() {
        ContentResolver contentResolver = getContentResolver();
        String contactId;
        String displayName;
        contactsListItems = new ArrayList<>();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    ContactsInfoClass contactsInfoClass = new ContactsInfoClass();
                    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    contactsInfoClass.setDisplayName(displayName);

                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    if (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s", "");

                        contactsInfoClass.setPhoneNumber(phoneNumber);
                    }

                    phoneCursor.close();

                    contactsListItems.add(contactsInfoClass);
                }
            }
        }
        cursor.close();

        if (!contactsListItems.isEmpty()) {
            try {
                checkContactsAgainstFirestoreUsers();
            } catch (Exception e) {
                //Error caught with no action
            }
        }

    }

    public String chatIdGenerator() {
        int stringLength = 20;
        ArrayList<Character> selectionSource = new ArrayList<Character>();
        ArrayList<Character> arrayOfRandomCharacters = new ArrayList<Character>();
        String caps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "1234567890";

        //Append items in each string to arraylist
        for (char i : caps.toCharArray()) {
            selectionSource.add(i);
        }

        for (char i : small.toCharArray()) {
            selectionSource.add(i);
        }

        for (char i : numbers.toCharArray()) {
            selectionSource.add(i);
        }
        // System.out.println(selectionSource);
        for (int i = 0; i < stringLength; i++) {
            Random rand = new Random();
            // Obtain a number between [0 - 9].
            int n = rand.nextInt(selectionSource.size());
            arrayOfRandomCharacters.add(selectionSource.get(n));
        }
        StringBuilder sb = new StringBuilder();
        for (char i : arrayOfRandomCharacters) {
            sb.append(i);
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Creates the menu inflater
        getMenuInflater().inflate(R.menu.new_conversation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handles clicks on clicked menu items
        int id = item.getItemId();

        if (id == R.id.refresh_contacts) {
            //Code to refresh contacts
            //Show snack bar with message
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "Refreshing contacts...", Snackbar.LENGTH_LONG).show();


            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    handler.postDelayed(this, 10);
                    getContacts();
                }
            };

        }
        return true;
    }

    private void checkContactsAgainstFirestoreUsers() {
        db.collection(userInfoCollection)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (ContactsInfoClass contact : contactsListItems) {
                                String phoneNumber = contact.getPhoneNumber();

                                //Replace phone number string if it starts with 0 with +254
                                if (phoneNumber.charAt(0) == '0') {
                                    phoneNumber = phoneNumber.replaceFirst("0", "+254");
                                }


                                for (DocumentSnapshot d : list) {
                                    if (Objects.requireNonNull(d.getString("User Phone")).equals(phoneNumber)) {
                                        saveContactsToFirestore(contact);
                                    }
                                }
                            }


                        }
                    }
                });
    }

    private void updatesListener() {
        dbContactsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                getContactsFromFirestore();
            }
        });
    }

    private void saveContactsToFirestore(ContactsInfoClass contactObject) {
        dbContactsCollection.document(contactObject.getPhoneNumber())
                .set(contactObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Do stuff
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Do stuff
                    }
                });
    }

    private void getContactsFromFirestore() {
        dbContactsCollection
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            contactsOnTuchatFromFireStore.clear();


                            for (DocumentSnapshot d : list) {
                                ContactsInfoClass p = d.toObject(ContactsInfoClass.class);


                                contactsOnTuchatFromFireStore.add(p);
                            }


                            adapter = new ContactsAdapter(contactsOnTuchatFromFireStore, NewConversationActivity.this);
                            newConversationRecyclerView.setAdapter(adapter);


                            newConversationRecyclerView.setVisibility(View.VISIBLE);
                            progressBarLayout.setVisibility(View.GONE);
                            placeHolderLayout.setVisibility(View.GONE);
                        } else {
                            newConversationRecyclerView.setVisibility(View.GONE);
                            progressBarLayout.setVisibility(View.GONE);
                            placeHolderLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void nextActivityCaller(String phoneNumber) {
        String generatedString = chatIdGenerator();

        Intent startConversation = new Intent(NewConversationActivity.this, ChatsActivity.class);
        startConversation.putExtra("Phone Number", phoneNumber);
        startConversation.putExtra("Chat ID", generatedString);
        startActivity(startConversation);

        //Animate transition into called activity
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        finish();
    }

    @Override
    public void finish() {
        //This method specifies the actions to do if the user presses back button while in this activity
        super.finish();

        //Animate transition to calling activity
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Animate transition into called activity
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
