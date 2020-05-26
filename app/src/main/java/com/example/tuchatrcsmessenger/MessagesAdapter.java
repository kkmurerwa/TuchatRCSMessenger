package com.example.tuchatrcsmessenger;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<MessagesClass> listItems;
    private Context context;
    private String myName;
    private Date currentDate;
    private String messageSenderName;
    private String serverTimestamp;
    private SimpleDateFormat formatterMessageTime = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatterDate = new SimpleDateFormat("dd-MM-yyyy");

    public MessagesAdapter(List<MessagesClass> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
        this.currentDate = new Date();
        this.currentDate.getTime();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessagesClass listItem = listItems.get(position);


        messageSenderName = listItem.senderName;
        serverTimestamp = formatterMessageTime.format(listItem.sentTime);

        if (!messageSenderName.equals(myName)){
            holder.messageBodySent.setText(listItem.getMessageBody());
            holder.messageSentTime.setText(serverTimestamp);
            holder.sentCL.setVisibility(View.VISIBLE);
            holder.receivedCL.setVisibility(View.GONE);
        }
        else {
            holder.messageBodyReceived.setText(listItem.getMessageBody());
            holder.messageReceivedTime.setText(serverTimestamp);
            holder.sentCL.setVisibility(View.GONE);
            holder.receivedCL.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView messageSentTime;
        public TextView messageReceivedTime;
        public TextView messageBodySent;
        public TextView messageBodyReceived;
        public TextView datePillText;
        public ConstraintLayout sentCL;
        public ConstraintLayout receivedCL;


        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(itemView.getContext(), "Heeeey", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            myName = ((ChatsActivity) context).returnMyName();


            messageBodySent = itemView.findViewById(R.id.message_body_sent_text_message);
            messageBodyReceived = itemView.findViewById(R.id.message_body_received_text_message);

            sentCL = itemView.findViewById(R.id.sent_message_layout);
            receivedCL = itemView.findViewById(R.id.received_message_layout);

            messageSentTime = itemView.findViewById(R.id.time_message_sent);
            messageReceivedTime = itemView.findViewById(R.id.time_message_received);

        }
    }
}
