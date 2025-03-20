package com.app.jamis;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationModel> notificationsList;
    private DatabaseReference usersRef;
    private Context context;

    public NotificationAdapter(List<NotificationModel> notificationsList) {
        this.notificationsList = notificationsList;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notificationsList.get(position);
        holder.jobTitleTextView.setText("JobCategory: " + notification.getJobCategory());
        holder.statusTextView.setText(notification.getStatus());
        holder.messageTextView.setText(notification.getMessage());
        holder.nameTextview.setText("Applicant: " + notification.getemployeeName());

        String timestamp = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm")
                .format(new java.util.Date(notification.getTimestamp()));
        holder.timeTextview.setText("Applied on: " + timestamp);


        // Set different colors or styles depending on the status
        if (notification.getStatus().equals("accepted")) {
            holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        } else {
            holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
    public void addNewJobApplication(NotificationModel newnotification) {
        notificationsList.add(0, newnotification);
        notifyItemInserted(0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView jobTitleTextView, statusTextView, messageTextView,nameTextview, timeTextview;

        public ViewHolder(View itemView) {
            super(itemView);
            jobTitleTextView = itemView.findViewById(R.id.jobTitleTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            nameTextview = itemView.findViewById(R.id.nameTextView);
            timeTextview = itemView.findViewById(R.id.timeTextView);

        }
    }
}
