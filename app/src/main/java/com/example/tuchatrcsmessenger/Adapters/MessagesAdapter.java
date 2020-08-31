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
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MessagesClass> listItems;
    private Context context;
    private String myName;
    private SimpleDateFormat formatterMessageTime = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatterDate = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat formatterHalfDate = new SimpleDateFormat("MMM yyyy");
    private SimpleDateFormat formatterDay = new SimpleDateFormat("dd");
    Date today;
    MessagesClass previousListItem;

    public MessagesAdapter(List<MessagesClass> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message_item, parent, false);
            return new SentViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receive_message_item, parent, false);
        return new ReceivedViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        // return super.getItemViewType(position);
        MessagesClass messagesClass = listItems.get(position);

        if (messagesClass.userId.equals(FirebaseAuth.getInstance().getUid())) {
            return 1;
        }

        return 2;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessagesClass listItem = listItems.get(position);


        if (holder.getItemViewType() == 1) {
            ((SentViewHolder) holder).bind(listItem, position);
        } else {
            ((ReceivedViewHolder) holder).bind(listItem, position);
        }

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    class SentViewHolder extends RecyclerView.ViewHolder {

        TextView messageSentTime;

        TextView messageBodySent;

        TextView datePillText;

        LinearLayout datePillLL;


        SentViewHolder(@NonNull final View itemView) {
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

            datePillText = itemView.findViewById(R.id.date_pill_text);

            datePillLL = itemView.findViewById(R.id.date_pill_linear_layout);


            messageSentTime = itemView.findViewById(R.id.time_message_sent);


        }

        void bind(MessagesClass listItem, int position) {
            today = new Date();
            today.getTime();

            String currentMessageDate = formatterDate.format(listItem.sentTime);
            String currentMessageHalfDate = formatterHalfDate.format(listItem.sentTime);
            String currentUserHalfDate = formatterHalfDate.format(today);
            String currentMessageDay = formatterDay.format(listItem.sentTime);
            String currentUserDay = formatterDay.format(today);


            Boolean datesMatch = currentMessageHalfDate.equals(currentUserHalfDate);
            Boolean today = currentMessageDay.equals(currentUserDay);
            Boolean yesterday = Integer.parseInt(currentMessageDay) == Integer.parseInt(currentUserDay) - 1;

            if (position >= 1) {
                previousListItem = listItems.get(position - 1);
                String previousMessageDate = formatterDate.format(previousListItem.sentTime);


                if (!currentMessageDate.equals(previousMessageDate)) {
                    datePillLL.setVisibility(View.VISIBLE);
                    if (today && datesMatch) {
                        datePillText.setText("Today");
                    } else if (yesterday && datesMatch) {
                        datePillText.setText("Yesterday");
                    } else {
                        datePillText.setText(currentMessageDate);
                    }
                } else {
                    datePillLL.setVisibility(View.GONE);
                }
            } else {
                datePillLL.setVisibility(View.VISIBLE);
                if (today && datesMatch) {
                    datePillText.setText("Today");
                } else if (yesterday && datesMatch) {
                    datePillText.setText("Yesterday");
                } else {
                    datePillText.setText(currentMessageDate);
                }
            }

            messageBodySent.setText(listItem.messageBody);
            messageSentTime.setText(formatterMessageTime.format(listItem.sentTime));

            String messageSenderName = listItem.senderName;
            String serverTimestamp = formatterMessageTime.format(listItem.sentTime);
        }
    }

    class ReceivedViewHolder extends RecyclerView.ViewHolder {

        TextView messageReceivedTime;
        TextView messageBodyReceived;
        TextView datePillText;
        LinearLayout datePillLL;


        ReceivedViewHolder(@NonNull final View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(itemView.getContext(), "Heeeey", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            myName = ((ChatsActivity) context).returnMyName();


            messageBodyReceived = itemView.findViewById(R.id.received_message_body);
            datePillText = itemView.findViewById(R.id.received_date_pill_text);


            datePillLL = itemView.findViewById(R.id.received_date_pill_linear_layout);

            messageReceivedTime = itemView.findViewById(R.id.received_message_time);

        }

        void bind(MessagesClass listItem, int position) {
            today = new Date();
            today.getTime();

            String currentMessageDate = formatterDate.format(listItem.sentTime);
            String currentMessageHalfDate = formatterHalfDate.format(listItem.sentTime);
            String currentUserHalfDate = formatterHalfDate.format(today);
            String currentMessageDay = formatterDay.format(listItem.sentTime);
            String currentUserDay = formatterDay.format(today);


            Boolean datesMatch = currentMessageHalfDate.equals(currentUserHalfDate);
            Boolean today = currentMessageDay.equals(currentUserDay);
            Boolean yesterday = Integer.parseInt(currentMessageDay) == Integer.parseInt(currentUserDay) - 1;

            if (position >= 1) {
                previousListItem = listItems.get(position - 1);
                String previousMessageDate = formatterDate.format(previousListItem.sentTime);


                if (!currentMessageDate.equals(previousMessageDate)) {
                    datePillLL.setVisibility(View.VISIBLE);
                    if (today && datesMatch) {
                        datePillText.setText("Today");
                    } else if (yesterday && datesMatch) {
                        datePillText.setText("Yesterday");
                    } else {
                        datePillText.setText(currentMessageDate);
                    }
                } else {
                    datePillLL.setVisibility(View.GONE);
                }
            } else {
                datePillLL.setVisibility(View.VISIBLE);
                if (today && datesMatch) {
                    datePillText.setText("Today");
                } else if (yesterday && datesMatch) {
                    datePillText.setText("Yesterday");
                } else {
                    datePillText.setText(currentMessageDate);
                }
            }
            messageBodyReceived.setText(listItem.messageBody);
            messageReceivedTime.setText(formatterMessageTime.format(listItem.sentTime));

            String messageSenderName = listItem.senderName;
            String serverTimestamp = formatterMessageTime.format(listItem.sentTime);
        }
    }
}