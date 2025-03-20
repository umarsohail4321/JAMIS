package com.app.jamis;



import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class employerNotiFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView empty;
    private JobNotiAdapter adapter; // Use the correct adapter name
    private List<EmployerNotificationModel> notificationList; // Correct model class
    private DatabaseReference notificationsRef;
    private FirebaseAuth auth;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employer_noti, container, false);

        recyclerView = view.findViewById(R.id.jobnotiRecyclerView);
        empty = view.findViewById(R.id.empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        adapter = new JobNotiAdapter(notificationList, getContext()); // Pass both the list and context to the adapter
        recyclerView.setAdapter(adapter);
        checkNotificationPermission();


        // Get Firebase Authentication instance
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentEmployerID = currentUser.getUid(); // Get current user's UID

            // Reference to the current employer's notifications in the database
            notificationsRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentEmployerID).child("Notifications");

            // Fetch notifications from Firebase
            notificationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    notificationList.clear();
                    if(snapshot.exists()){
                        empty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        EmployerNotificationModel notification = notificationSnapshot.getValue(EmployerNotificationModel.class);
                        if (notification != null) {
                            notificationList.add(0, notification);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    }else{
                        empty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
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
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                // Permission already granted
                notifyAdapterPermissionGranted(true);
            }
        } else {
            // For older versions, assume permission is granted
            notifyAdapterPermissionGranted(true);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                notifyAdapterPermissionGranted(true);
            } else {
                // Permission denied
                notifyAdapterPermissionGranted(false);
            }
        }
    }
    private void notifyAdapterPermissionGranted(boolean isGranted) {
        if (adapter != null) {
            adapter.setPermissionGranted(isGranted);
        }
    }
}
