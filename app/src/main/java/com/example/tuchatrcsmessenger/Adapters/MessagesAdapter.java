package com.example.tuchatrcsmessenger.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuchatrcsmessenger.ChatsActivity;
import com.example.tuchatrcsmessenger.Classes.MessagesClass;
import com.example.tuchatrcsmessenger.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<MessagesClass> listItems;
    private Context context;
    private String myName;
    private SimpleDateFormat formatterMessageTime = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatterDate = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat formatterHalfDate = new SimpleDateFormat("MMM yyyy");
    private SimpleDateFormat formatterDay = new SimpleDateFormat("dd");
    Date today;

    public MessagesAdapter(List<MessagesClass> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
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
        MessagesClass previousListItem;
        today = new Date();
        today.getTime();

        String currentMessageDate = formatterDate.format(listItem.sentTime);
        String currentMessageHalfDate = formatterHalfDate.format(listItem.sentTime);
        String currentUserHalfDate = formatterHalfDate.format(today);
        String currentMessageDay = formatterDay.format(listItem.sentTime);
        String currentUserDay = formatterDay.format(today);


        Boolean datesMatch = currentMessageHalfDate.equals(currentUserHalfDate);
        Boolean today = currentMessageDay.equals(currentUserDay);
        Boolean yesterday = Integer.parseInt(currentMessageDay) == Integer.parseInt(currentUserDay)-1;

        if (position >= 1){
            previousListItem = listItems.get(position-1);
            String previousMessageDate = formatterDate.format(previousListItem.sentTime);


            if (!currentMessageDate.equals(previousMessageDate)){
                holder.datePillLL.setVisibility(View.VISIBLE);
                if (today && datesMatch){
                    holder.datePillText.setText("Today");
                }
                else if (yesterday && datesMatch){
                    holder.datePillText.setText("Yesterday");
                }
                else {
                    holder.datePillText.setText(currentMessageDate);
                }
            }
            else {
                holder.datePillLL.setVisibility(View.GONE);
            }
        }
        else {
            holder.datePillLL.setVisibility(View.VISIBLE);
            if (today && datesMatch){
                holder.datePillText.setText("Today");
            }
            else if (yesterday && datesMatch){
                holder.datePillText.setText("Yesterday");
            }
            else {
                holder.datePillText.setText(currentMessageDate);
            }
        }


        String messageSenderName = listItem.senderName;
        String serverTimestamp = formatterMessageTime.format(listItem.sentTime);

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



    class ViewHolder extends RecyclerView.ViewHolder{

        TextView messageSentTime;
        TextView messageReceivedTime;
        TextView messageBodySent;
        TextView messageBodyReceived;
        TextView datePillText;
        ConstraintLayout sentCL;
        ConstraintLayout receivedCL;
        LinearLayout datePillLL;


        ViewHolder(@NonNull final View itemView) {
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
            datePillText = itemView.findViewById(R.id.date_pill_text);

            sentCL = itemView.findViewById(R.id.sent_message_layout);
            receivedCL = itemView.findViewById(R.id.received_message_layout);
            datePillLL = itemView.findViewById(R.id.date_pill_linear_layout);


            messageSentTime = itemView.findViewById(R.id.time_message_sent);
            messageReceivedTime = itemView.findViewById(R.id.time_message_received);

        }
    }
}
