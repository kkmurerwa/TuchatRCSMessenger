package com.example.tuchatrcsmessenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewConversationActivity extends AppCompatActivity {

    //Test activity variables
    Button newConvButt;
    EditText phonNo;
    String phoneNumber;

    private List<ConversationsClass> contactsListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);

        //Set action bar Title
        setTitle("Start conversation");

        newConvButt = findViewById(R.id.start_conversation_button);
        phonNo = findViewById(R.id.test_phone_entry);


        newConvButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = phonNo.getText().toString().trim();

                String generatedString = chatIdGenerator();

                Intent startConversation = new Intent(NewConversationActivity.this, ChatsActivity.class);
                startConversation.putExtra("Phone Number", phoneNumber);
                startConversation.putExtra("Chat ID", generatedString);
                startActivity(startConversation);
                finish();
            }
        });

    }

    public String chatIdGenerator(){
        int stringLength = 20;
        ArrayList <Character> selectionSource = new ArrayList<Character>();
        ArrayList <Character> arrayOfRandomCharacters = new ArrayList<Character>();
        String caps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "1234567890";

        //Append items in each string to arraylist
        for (char i:caps.toCharArray()){
            selectionSource.add(i);
        }

        for (char i:small.toCharArray()){
            selectionSource.add(i);
        }

        for (char i:numbers.toCharArray()){
            selectionSource.add(i);
        }
        // System.out.println(selectionSource);
        for(int i = 0; i<stringLength; i++){
            Random rand = new Random();
            // Obtain a number between [0 - 9].
            int n = rand.nextInt(selectionSource.size());
            arrayOfRandomCharacters.add(selectionSource.get(n));
        }
        StringBuilder sb = new StringBuilder();
        for (char i: arrayOfRandomCharacters){
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

            //Show snackbar with message
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "Refreshing contacts...", Snackbar.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void finish() {
        //This method specifies the actions to do if the user presses back button while in this activity
        super.finish();

        //Animate transition to calling activity
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
