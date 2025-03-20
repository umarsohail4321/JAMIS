package com.app.jamis;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<JobModel> jobList = new ArrayList<>();
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private ImageView noInternetImageView;
    private NetworkChangeReceiver networkChangeReceiver;
    private EditText search;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        recyclerView = view.findViewById(R.id.jobRecyclerView);
        search = view.findViewById(R.id.search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        noInternetImageView = view.findViewById(R.id.noInternetImageView);

        networkChangeReceiver = new NetworkChangeReceiver(noInternetImageView);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireActivity().registerReceiver(networkChangeReceiver, filter);


        checkInternetAndLoadData();
        checkNotificationPermission();


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    searchFilter(s.toString().toLowerCase());
                }else{
                    jobAdapter.setJobList(jobList);
                }
            }
        });


        return view;
    }

    private void searchFilter(String string) {
        ArrayList<JobModel> filteredJobs = new ArrayList<>();
        for (JobModel model: jobList){
            if (model.getCity().toLowerCase().contains(string) ||
                    model.getCountry().toLowerCase().contains(string) || model.getJobCategory().toLowerCase().contains(string) ||
                    model.getDesignation().toLowerCase().contains(string)){
                filteredJobs.add(model);
            }
        }
        jobAdapter.setJobList(filteredJobs);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the receiver to avoid memory leaks
        requireActivity().unregisterReceiver(networkChangeReceiver);
    }

    private void checkInternetAndLoadData() {
        // Check if the device is connected to the internet
        if (NetworkUtil.isConnected(getContext())) {
            // Internet is available, show RecyclerView and fetch data
            noInternetImageView.setVisibility(View.GONE);  // Hide "No Internet" image
            recyclerView.setVisibility(View.VISIBLE);      // Show RecyclerView

            // Fetch jobs from Firebase
            DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
            jobsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    jobList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            JobModel job = ds.getValue(JobModel.class);  // Get job model from Firebase
                            jobList.add(job);  // Add to job list
                        }
                        jobAdapter.notifyDataSetChanged();  // Notify adapter about data change
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });

            // Set up the adapter
            jobAdapter = new JobAdapter(getActivity(), jobList);  // Pass jobList from Firebase
            recyclerView.setAdapter(jobAdapter);

        } else {
            // No internet connection, show "No Internet" image and hide RecyclerView
            noInternetImageView.setVisibility(View.VISIBLE);  // Show "No Internet" image
            recyclerView.setVisibility(View.GONE);           // Hide RecyclerView
        }
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
    @Override
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
        if (jobAdapter != null) {
            jobAdapter.setPermissionGranted(isGranted);  // Pass the permission status to the adapter
        }
    }
}