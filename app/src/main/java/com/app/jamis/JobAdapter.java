package com.app.jamis;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<JobModel> jobList;
    private DatabaseReference usersRef, jobsRef; // Firebase references for fetching data
    private boolean isPermissionGranted = false;

    public JobAdapter(Context context, List<JobModel> jobList) {
        this.context = context;
        this.jobList = jobList;
        this.usersRef = FirebaseDatabase.getInstance().getReference("Users");
        this.jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
    }

    public void setJobList(List<JobModel> jobList) {
        this.jobList = jobList;
        notifyDataSetChanged();
    }

    public void setPermissionGranted(boolean granted) {
        isPermissionGranted = granted;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.job_item, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobModel job = jobList.get(position);

        // Set job details in the item view
        holder.companyNameTextView.setText(job.getCompanyName());
        holder.jobCategoryTextView.setText(job.getJobCategory());
        holder.designationTextView.setText(job.getDesignation());
        holder.skillsTextView.setText(job.getSkills());




        FirebaseDatabase.getInstance().getReference("Users").child(job.getEmployerID())
                        .child("Notifications")
                                .orderByChild("jobId").equalTo(job.getJobID())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    holder.applyButton.setBackgroundResource(R.drawable.applybtn);
                                    holder.applyButton.setTextColor(ContextCompat.getColor(context, R.color.green));
                                    holder.applyButton.setText("Applied");
                                    holder.applyButton.setEnabled(false);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


        FirebaseDatabase.getInstance().getReference("Users")
                .child(job.getEmployerID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.child("image").exists()){
                                Glide.with(context).load(
                                        snapshot.child("image").getValue(String.class)
                                ).into(holder.companyProfilePicImageView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.applyButton.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                Toast.makeText(context, "Please login before applying.", Toast.LENGTH_SHORT).show();
                return;
            }
            String employeeID = FirebaseAuth.getInstance().getUid();

            if (employeeID == null) {
                Log.e("JobAdapter", "Employee ID is null");
                Toast.makeText(context, "Please log in to apply for jobs.", Toast.LENGTH_SHORT).show();
                return;
            }
            holder.applyButton.setBackgroundResource(R.drawable.applybtn);
            holder.applyButton.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.applyButton.setText("Applied");
            holder.applyButton.setEnabled(false);

            Toast.makeText(context, "Successfully applied to the job!", Toast.LENGTH_SHORT).show();
            // Fetch current employee's ID


            // Fetch job ID and employer ID from job object
            String jobID = job.getJobID();
            String employerID = job.getEmployerID(); // Employer ID from the jobs node

            // Fetch employee's name from "users" node
            usersRef.child(employeeID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String employeeName = snapshot.child("fullName").getValue(String.class);

                        // Fetch job details from "jobs" node (like jobCategory)
                        jobsRef.child(jobID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot jobSnapshot) {
                                if (jobSnapshot.exists()) {
                                    String jobCategory = jobSnapshot.child("jobCategory").getValue(String.class);

                                    // Fetch FCM token for employer from the "users" node
                                    DatabaseReference employerRef = FirebaseDatabase.getInstance()
                                            .getReference("Users") // Fetch employer info from "users" node
                                            .child(employerID)
                                            .child("fcmToken");
                                    Log.d("JobAdapter","employerref"+employerRef);

                                    employerRef.get().addOnCompleteListener(fcmTokenTask -> {
                                        if (fcmTokenTask.isSuccessful()) {
                                            String fcmToken = fcmTokenTask.getResult().getValue(String.class);

                                            if (fcmToken != null) {
                                                // Send notification to employer
                                                sendNotificationToEmployer(employerID, employeeName, jobCategory);

                                                // Add notification entry in Firebase under "users/employerID/Notifications"
                                                DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                                                        .getReference("Users") // Ensure this is under "users"
                                                        .child(employerID)
                                                        .child("Notifications");

                                                String notificationID = notificationsRef.push().getKey();
                                                Map<String, Object> notificationData = new HashMap<>();
                                                notificationData.put("employeeName", employeeName);
                                                notificationData.put("employeeID", employeeID);
                                                notificationData.put("jobCategory", jobCategory);
                                                notificationData.put("message", "New job application received!");
                                                notificationData.put("timestamp", System.currentTimeMillis());
                                                notificationData.put("jobId", jobID);
                                                notificationData.put("status", "pending");

                                                if (notificationID != null) {
                                                    notificationsRef.child(notificationID).setValue(notificationData)
                                                            .addOnSuccessListener(aVoid -> {
                                                                // Successfully added notification
                                                                Log.d("JobAdapter", "Notification sent to employer: " + employerID);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("JobAdapter", "Failed to send notification: " + e.getMessage());
                                                            });
                                                }
                                            } else {
                                                Log.e("JobAdapter", "FCM token for employer is null");
                                            }
                                        } else {
                                            Log.e("JobAdapter", "Failed to fetch FCM token: " + fcmTokenTask.getException());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("JobAdapter", "Failed to fetch job data: " + error.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("JobAdapter", "Failed to fetch employee data: " + error.getMessage());
                }
            });
        });



    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView companyNameTextView, jobCategoryTextView, designationTextView, skillsTextView;
        ImageView companyProfilePicImageView;
        Button applyButton;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            companyNameTextView = itemView.findViewById(R.id.companyName);
            jobCategoryTextView = itemView.findViewById(R.id.jobTitle);
            designationTextView = itemView.findViewById(R.id.designation2);
            skillsTextView = itemView.findViewById(R.id.skills2);
            companyProfilePicImageView = itemView.findViewById(R.id.companylogo);
            applyButton = itemView.findViewById(R.id.applybtn);
        }
    }

    // Method to send FCM Notification to the Employer
    private void sendNotificationToEmployer(String employerID, String employeeName, String jobCategory) {
        if (!isPermissionGranted) {
            Log.d("NotificationPermission", "Notification permission not granted");
            return;
        }
        // Fetch employer's FCM token from Firebase
        usersRef.child(employerID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String employerToken = snapshot.getValue(String.class);


                    // Call the FCM sender class to send the notification
                    try {
                        // Send new job application notification to the employer
                        FcmSender.sendNotification(
                                employerToken,       // Recipient token (employer's FCM token)
                                employeeName,        // Employee's name
                                jobCategory,         // Job category
                                "job_application",   // Type of notification
                                null
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
