package com.example.tuchatrcsmessenger.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuchatrcsmessenger.ChatsActivity;
import com.example.tuchatrcsmessenger.Classes.messagesClass;
import com.example.tuchatrcsmessenger.R;
import com.example.tuchatrcsmessenger.data.db.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Date today;
    messagesClass previousListItem;
    private Context context;
    private Integer currentPosition = null;
    private String myName;
    private List<messagesClass> listItems;
    private SimpleDateFormat formatterMessageTime = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatterDate = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat formatterHalfDate = new SimpleDateFormat("MMM yyyy");
    private SimpleDateFormat formatterDay = new SimpleDateFormat("dd");
    private final AppDatabase mDb;
    private int mUnreadCount;

    public MessagesAdapter(Context context) {
        this.context = context;
        listItems = new ArrayList<>();
        mDb = AppDatabase.getInstance(context);
        mUnreadCount = ((ChatsActivity) context).getUnreadCount();
    }

    public void setList(List<messagesClass> newList) {
        listItems.clear();
        listItems.addAll(newList);
    }

    public void setCurrentPosition(Integer position) {
        currentPosition = position;
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
        messagesClass messagesClass = listItems.get(position);

        if (messagesClass.userId.equals(FirebaseAuth.getInstance().getUid())) {
            return 1;
        }

        return 2;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        messagesClass listItem = listItems.get(position);
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

        ProgressBar sentMessageProgress;


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

            sentMessageProgress = itemView.findViewById(R.id.sent_progress);

            messageSentTime = itemView.findViewById(R.id.time_message_sent);


        }

        void bind(messagesClass listItem, int position) {
            today = new Date();
            today.getTime();

            ((ChatsActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sentMessageProgress.setVisibility(View.GONE);
                }
            });

            if (currentPosition != null) {

                if (currentPosition == getAdapterPosition()) {
                    sentMessageProgress.setVisibility(View.VISIBLE);
                    ((ChatsActivity) context).setCurrentProgress(sentMessageProgress);
                }

            }

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
        TextView unreadCountPill;
        LinearLayout datePillLL;
        LinearLayout unreadCountPillLL;


        ReceivedViewHolder(@NonNull final View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // TODO Add code to copy text to clipboard
                    Toast.makeText(itemView.getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            myName = ((ChatsActivity) context).returnMyName();



            messageBodyReceived = itemView.findViewById(R.id.received_message_body);
            datePillText = itemView.findViewById(R.id.received_date_pill_text);
            unreadCountPill = itemView.findViewById(R.id.received_unread_count);


            datePillLL = itemView.findViewById(R.id.received_date_pill_linear_layout);
            unreadCountPillLL = itemView.findViewById(R.id.received_unread_count_linear_layout);

            messageReceivedTime = itemView.findViewById(R.id.received_message_time);

        }

        void bind(messagesClass listItem, int position) {
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

//            if (mUnreadCount>0){
//                if (position == getItemCount()-mUnreadCount) {
//                    if (mUnreadCount>1){
//                        unreadCountPillLL.setVisibility(View.VISIBLE);
//                        unreadCountPill.setText(String.format("%s unread messages", mUnreadCount));
//                    } else {
//                        unreadCountPillLL.setVisibility(View.VISIBLE);
//                        unreadCountPill.setText(String.format("%s unread message", mUnreadCount));
//                    }
//                } else {
//                    unreadCountPillLL.setVisibility(View.GONE);
//                }
//            }

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
