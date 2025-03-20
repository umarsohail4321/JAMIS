package com.app.jamis;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationsList;
    private DatabaseReference notificationsRef;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize RecyclerView and adapter
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list of notifications
        notificationsList = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(notificationsList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentEmployeeID = currentUser.getUid(); // Get current user's UID

            // Reference to the current employer's notifications in the database
            notificationsRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentEmployeeID).child("Notifications");

            // Fetch notifications from Firebase
            notificationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    notificationsList.clear();
                    List<NotificationModel> tempList = new ArrayList<>();
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        NotificationModel notification = notificationSnapshot.getValue(NotificationModel.class);

                        if (notification != null) {
                            tempList.add(notification);
                        }
                    }
                    for (int i = tempList.size() - 1; i >= 0; i--) {
                        notificationsList.add(tempList.get(i));
                    }
                    notificationAdapter.notifyDataSetChanged();
                    notificationsRecyclerView.scrollToPosition(0);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("NotificationFragment", "Failed to fetch notifications: " + error.getMessage());
                }
            });
        } else {
            Log.e("NotificationFragment", "No authenticated user found");
        }

        return view;
    }
}
