package com.example.tuchatrcsmessenger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.Log;
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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuchatrcsmessenger.Adapters.ContactsAdapter;
import com.example.tuchatrcsmessenger.Classes.ContactsInfoClass;
import com.example.tuchatrcsmessenger.data.db.AppDatabase;
import com.example.tuchatrcsmessenger.data.entity.ContactsClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class NewConversationActivity extends AppCompatActivity {
    //Contact permission variables
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    //Firebase path variables
    final String userInfoCollection = "users";
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    //Firestore Path Variables
    private String contactsCollection = "contacts";
    private String userID;
    private String myContactsSubcollection = "My Contacts";
    //New Conversations activity variables
    private RecyclerView newConversationRecyclerView;
    private LinearLayout progressBarLayout;
    private LinearLayout placeHolderLayout;
    private int dismissStatus = 0;

    private List<ContactsClass> contactsListItems;
    private List<ContactsClass> contactsToBeSaved;
    private List<ContactsInfoClass> contactsOnTuchatListItem;
    private CollectionReference dbContactsCollection;
    private List<ContactsInfoClass> contactsOnTuchatFromFireStore;
    private ContactsAdapter adapter;

    private AppDatabase appDatabase;
    private Boolean dontHideProgress = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);

        //Initialize and display toolbar
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

        appDatabase = AppDatabase.getInstance(this);

        setAdapter();
        getSavedContacts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContacts();
    }

    public void requestContactsPermission() {
        int readContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (readContacts != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
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
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, Phone.DISPLAY_NAME + " ASC");
        assert cursor != null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    Cursor phoneCursor = getContentResolver().query(
                            Phone.CONTENT_URI,
                            null,
                            Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    assert phoneCursor != null;
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER)).replaceAll("\\s", "");

                        //Replace phone number string if it starts with 0 with +254
                        if (phoneNumber.charAt(0) == '0') {
                            phoneNumber = phoneNumber.replaceFirst("0", "+254");
                        }
                        ContactsClass contactsClass = new ContactsClass();
                        contactsClass.setDisplayName(displayName);
                        contactsClass.setPhoneNumber(phoneNumber);
                        contactsListItems.add(contactsClass);
                    }
                    phoneCursor.close();

                }
            }
        }
        cursor.close();
        checkContactsAgainstFirestoreUsers();
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
            refreshContacts();
        }
        return true;
    }

    private void refreshContacts() {
        placeHolderLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
        dontHideProgress = true;
        //Delete Current Db
        new DeleteDb(NewConversationActivity.this).execute();

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "Refreshing contacts...", Snackbar.LENGTH_LONG).show();

        new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
                //Add code after every second
            }

            public void onFinish() {

                Toast.makeText(NewConversationActivity.this, "I have been called", Toast.LENGTH_SHORT).show();
                getContacts();
            }
        }.start();
    }

    private void checkContactsAgainstFirestoreUsers() {
        db.collection(userInfoCollection)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d("SearchContacts", queryDocumentSnapshots.toString());
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            boolean checkIfEvenOneExist = false;

                            if (!list.isEmpty()) {
                                for (int i = 0; i < contactsListItems.size(); i++) {
                                    for (DocumentSnapshot d : list) {
                                        if (Objects.equals(contactsListItems.get(i), "+254731240085")){
                                            Toast.makeText(NewConversationActivity.this, "Sophie found!!!", Toast.LENGTH_SHORT).show();
                                        }

                                        if (Objects.requireNonNull(d.getString("user_phone")).equals(contactsListItems.get(i).getPhoneNumber())) {
                                            savetoDB(contactsListItems.get(i));
                                            checkIfEvenOneExist = true;
                                        }
                                    }
                                }
                                if (!checkIfEvenOneExist) {
                                    setListEmpty();
                                }
                            } else {
                                setListEmpty();
                            }
                        } else {
                            setListEmpty();
                        }
                    }
                });
    }

    private void setListEmpty() {
        progressBarLayout.setVisibility(View.GONE);
        placeHolderLayout.setVisibility(View.VISIBLE);
    }

    private void getSavedContacts() {
        new RetrieveContacts(NewConversationActivity.this).execute();
    }

    private void savetoDB(ContactsClass contactsClass) {
        new InsertTask(this, contactsClass).execute();
    }

    public void setAdapter() {
        adapter = new ContactsAdapter(NewConversationActivity.this);
        newConversationRecyclerView.setAdapter(adapter);
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


    /**
     * The following classes are used by java to do actions in background. Since we are accessing db we must do everything in
     * background.
     */


    //Retrieving contacts

    public static class RetrieveContacts extends AsyncTask<Void, Void, LiveData<List<ContactsClass>>> {

        private WeakReference<NewConversationActivity> activityReference;

        RetrieveContacts(NewConversationActivity context) {
            activityReference = new WeakReference<>(context);
        }


        @Override
        protected LiveData<List<ContactsClass>> doInBackground(Void... voids) {
            if (activityReference.get() != null)
                return activityReference.get().appDatabase.getContactsDao().getContacts();
            else
                return null;

        }

        @Override
        protected void onPostExecute(LiveData<List<ContactsClass>> contactsClassesLiveData) {
            super.onPostExecute(contactsClassesLiveData);

            contactsClassesLiveData.observe(activityReference.get(), new Observer<List<ContactsClass>>() {
                @Override
                public void onChanged(List<ContactsClass> contactsClasses) {

                    if (contactsClasses != null && !contactsClasses.isEmpty()) {

                        activityReference.get().adapter.setListItems(contactsClasses);
                        activityReference.get().progressBarLayout.setVisibility(View.GONE);
                        activityReference.get().placeHolderLayout.setVisibility(View.GONE);
                        activityReference.get().newConversationRecyclerView.setVisibility(View.VISIBLE);

                    } else {
                        activityReference.get().newConversationRecyclerView.setVisibility(View.GONE);


                        if (activityReference.get().dontHideProgress) {
                            activityReference.get().progressBarLayout.setVisibility(View.VISIBLE);
                            activityReference.get().placeHolderLayout.setVisibility(View.GONE);
                            activityReference.get().dontHideProgress = false;
                        } else {
                            activityReference.get().progressBarLayout.setVisibility(View.GONE);
                            activityReference.get().placeHolderLayout.setVisibility(View.VISIBLE);
                        }
                    }


                }
            });


        }
    }

    //Adding contacts to db
    private static class InsertTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<NewConversationActivity> activityReference;
        private ContactsClass contactsClass;

        // only retain a weak reference to the activity
        InsertTask(NewConversationActivity context, ContactsClass contactsClass) {
            activityReference = new WeakReference<>(context);
            this.contactsClass = contactsClass;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            activityReference.get().appDatabase.getContactsDao().insertContact(contactsClass);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

    private static class DeleteDb extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<NewConversationActivity> activityReference;


        // only retain a weak reference to the activity
        DeleteDb(NewConversationActivity context) {
            activityReference = new WeakReference<>(context);

        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            activityReference.get().appDatabase.getContactsDao().deleteAll();
            return true;
        }


    }
}
