package com.example.tuchatrcsmessenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private List<ContactsInfo> listItems;
    private Context context;

    public ContactsAdapter(List<ContactsInfo> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactsInfo listItem = listItems.get(position);

        holder.contactDisplayName.setText(listItem.getDisplayName());
        holder.contactPhoneNumber.setText(listItem.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView contactDisplayName;
        public TextView contactPhoneNumber;
        public String chatID;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            contactDisplayName = itemView.findViewById(R.id.contact_display_name);
            contactPhoneNumber = itemView.findViewById(R.id.contact_phone_number);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();

            Toast.makeText(context, "I have been clicked", Toast.LENGTH_SHORT).show();
            String phoneNumber = listItems.get(pos).getPhoneNumber();

            ((NewConversationActivity) context).nextActivityCaller(phoneNumber);
        }

    }

}
