package com.app.jamis;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JobNotiAdapter extends RecyclerView.Adapter<JobNotiAdapter.JobApplicationViewHolder> {

    private List<EmployerNotificationModel> jobApplicationList;
    private Context context;
    private DatabaseReference usersRef;
    private boolean isPermissionGranted = false;

    public JobNotiAdapter(List<EmployerNotificationModel> jobApplicationList, Context context) {
        this.jobApplicationList = jobApplicationList;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("Users");
    }
    public void setPermissionGranted(boolean granted) {
        isPermissionGranted = granted;
    }

    @NonNull
    @Override
    public JobApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_noti, parent, false);
        return new JobApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobApplicationViewHolder holder, int position) {
        EmployerNotificationModel application = jobApplicationList.get(position);
        holder.applicantNameTextView.setText("Applicant: " + application.getEmployeeName());
        holder.messageTextView.setText(application.getMessage()); // Display custom message
        holder.jobCategoryTextView.setText("Job Category: " + application.getJobCategory());

        // Display timestamp (if you want to show the exact time)
        String timestamp = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm")
                .format(new java.util.Date(application.getTimestamp()));
        holder.timestampTextView.setText("Applied on: " + timestamp);

        // Check status and show/hide buttons accordingly
        if (!application.getStatus().equalsIgnoreCase("pending")) {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.statusTextView.setText(application.getStatus()); // Show status
            holder.statusTextView.setVisibility(View.VISIBLE);
        } else {
            holder.statusTextView.setVisibility(View.GONE);
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
        }

// Handle Accept button click with confirmation
        holder.acceptButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to accept this application?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sendNotificationToEmployee(application, "accepted");
                        holder.acceptButton.setVisibility(View.GONE);
                        holder.rejectButton.setVisibility(View.GONE);
                        holder.statusTextView.setText("Accepted");
                        holder.statusTextView.setVisibility(View.VISIBLE);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

// Handle Reject button click with confirmation
        holder.rejectButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to reject this application?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sendNotificationToEmployee(application, "rejected");
                        holder.acceptButton.setVisibility(View.GONE);
                        holder.rejectButton.setVisibility(View.GONE);
                        holder.statusTextView.setText("Rejected");
                        holder.statusTextView.setVisibility(View.VISIBLE);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

    }


    @Override
    public int getItemCount() {
        return jobApplicationList.size();
    }
    public void addNewJobApplication(EmployerNotificationModel newApplication) {
        jobApplicationList.add(0, newApplication);
        notifyItemInserted(0);
    }


    static class JobApplicationViewHolder extends RecyclerView.ViewHolder {
        TextView applicantNameTextView;
        TextView messageTextView;
        TextView jobCategoryTextView;
        TextView timestampTextView;
        TextView statusTextView;
        Button acceptButton;
        Button rejectButton;



        public JobApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            applicantNameTextView = itemView.findViewById(R.id.applicantNameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            jobCategoryTextView = itemView.findViewById(R.id.jobcategoryTextView); // Added for jobCategory
            timestampTextView = itemView.findViewById(R.id.timestampTextView); // Added for timestamp
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
    private void sendNotificationToEmployee(EmployerNotificationModel application, String status) {
        // Fetch the employeeID from the EmployerNotificationModel
        String employeeID = application.getEmployeeID();
        if (employeeID == null || employeeID.isEmpty()) {
            Log.e("JobNotiAdapter", "Employee ID is null or empty.");
            Toast.makeText(context, "Failed to send notification. Employee ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        String employerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch the FCM token from the Users node for the employee
        usersRef.child(employeeID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fcmToken = dataSnapshot.getValue(String.class);

                if (fcmToken != null) {
                    // Create notification payload to send to employee
                    String message = status.equals("accepted")
                            ? "Your application for the " + application.getJobCategory() + " has been accepted."
                            : "Your application for the " + application.getJobCategory() + " has been rejected.";

                    // Send FCM notification to employee
                    sendFCMNotification(fcmToken, application.getEmployeeName(), application.getJobCategory(), message, status);

                    // Create a new notification node for the employee
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("employeeName", application.getEmployeeName());
                    notificationData.put("jobCategory", application.getJobCategory());
                    notificationData.put("message", message);
                    notificationData.put("status", status);
                    notificationData.put("timestamp", System.currentTimeMillis());

                    // Add notification to the employee's node
                    usersRef.child(employeeID).child("Notifications").push().setValue(notificationData);
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Notifications")
                                    .child(application.getJobId()).child("status").setValue(status);

                    // Show a success message to the employer
                    Toast.makeText(context, "Notification sent to " + application.getEmployeeName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FCMToken", "Failed to fetch FCM token: " + error.getMessage());
            }
        });
    }
    private void sendFCMNotification(String employeeID, String employeeName, String jobCategory, String message, String status) {
        if (!isPermissionGranted) {
            Log.d("NotificationPermission", "Notification permission not granted");
            return;
        }
        // Fetch employee's FCM token from Firebase
        usersRef.child(employeeID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String employeeToken = snapshot.getValue(String.class);


                    // Call the FCM sender class to send the notification
                    try {
                        // Send new job application notification to the employer
                        FcmSender.sendNotification(
                                employeeToken,       // Recipient token (employer's FCM token)
                                employeeName,        // Employee's name
                                jobCategory,        // Job category
                                status,
                                "application_update" // Type of notification
                        );
                    } catch (Exception e) {
                        Log.d("NOTI_TAG", "onDataChange: Error: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JobAdapter", "Failed to fetch employer token: " + error.getMessage());
            }
        });
    }

}
