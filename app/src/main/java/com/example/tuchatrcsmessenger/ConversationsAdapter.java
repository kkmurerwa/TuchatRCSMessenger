package com.example.tuchatrcsmessenger;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    private List<ConversationsClass> listItems;
    private Context context;
    private Date currentDate;
    private SimpleDateFormat formatterFullDate = new SimpleDateFormat("d MMM yyyy");
    private SimpleDateFormat formatterHalfDate = new SimpleDateFormat("d MMM");
    private SimpleDateFormat formatterDay = new SimpleDateFormat("d");
    private SimpleDateFormat formatterYear = new SimpleDateFormat("yyyy");
    private String currentDateString;
    private String currentDateStringDay;
    private String currentDateStringYear;



    public ConversationsAdapter(List<ConversationsClass> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
        this.currentDate = new Date();
        this.currentDate.getTime();
        this.currentDateString = formatterFullDate.format(currentDate);
        this.currentDateStringDay = formatterDay.format(currentDate);
        this.currentDateStringYear = formatterYear.format(currentDate);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversation_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationsClass listItem = listItems.get(position);


        String strDate= formatterFullDate.format(listItem.getSentTime());

        String strDateDay = formatterDay.format(listItem.getSentTime());

        String strDateYear = formatterYear.format(listItem.getSentTime());


        if (strDate.equals(currentDateString) && strDateYear.equals(currentDateStringYear)){
            holder.sentTime.setText("Today");
        }
        else if (Integer.parseInt(strDateDay) == Integer.parseInt(currentDateStringDay)-1 && strDateYear.equals(currentDateStringYear)){
            holder.sentTime.setText("Yesterday");
        }
        else if (strDateYear.equals(currentDateStringYear)){
            holder.sentTime.setText(formatterHalfDate.format(listItem.getSentTime()));
        }
        else {
            holder.sentTime.setText(strDate);
        }

        holder.sender.setText(listItem.getSenderName());
        holder.messageBody.setText(listItem.getMessageBody());
        String readStatus = listItem.getReadStatus();

        //Show unread messages
        if (readStatus.equals("read")){
            //Do some stuff
            holder.readStatusButton.setBackgroundResource(R.drawable.notification_dot_read);
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView sender;
        public TextView sentTime;
        public TextView messageBody;
        public Button readStatusButton;
        public String readStatus;
        public String chatID;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            sender = itemView.findViewById(R.id.sender_name);
            sentTime = itemView.findViewById(R.id.sent_time);
            messageBody = itemView.findViewById(R.id.message_body);
            readStatusButton= itemView.findViewById(R.id.button);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();

            Intent openChatMessages = new Intent(context, ChatsActivity.class);
            openChatMessages.putExtra("Sender Name", this.sender.getText().toString());
            openChatMessages.putExtra("Chat ID", listItems.get(pos).chatRoomId);
            context.startActivity(openChatMessages);

            //Animate transition into called activity
            ((MainActivity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

    }

}
