package com.app.jamis;

import static com.app.jamis.R.id.jobformRecyclerView11;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class employerHomeFragment extends Fragment {

    private List<JobModel> jobList = new ArrayList<>();
    private JobFormAdapter jobFormAdapter;
    private RecyclerView jobRecyclerView;
    private ImageButton createJobBtn;
    private TextView employerName, companyName, employerDesignation;
    Context context;
    ValueEventListener profileListener;
    ImageView empty;
    ImageView profileImage;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employer_home, container, false);

        jobRecyclerView = view.findViewById(jobformRecyclerView11);
        createJobBtn = view.findViewById(R.id.createjob_btn);
        employerDesignation = view.findViewById(R.id.employerDesignation);
        employerName = view.findViewById(R.id.employerName);
        companyName = view.findViewById(R.id.companyName);
        empty = view.findViewById(R.id.empty);
        profileImage = view.findViewById(R.id.profileImage);

        context = requireContext();

        profileListener = FirebaseDatabase.getInstance().getReference("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            if (!snapshot.child("company").exists() ||
                                                    snapshot.child("company").getValue(String.class).isEmpty()){
                                                Toast.makeText(requireContext(), "Complete your profile first.", Toast.LENGTH_SHORT).show();
                                                Utils.replaceFragment(requireActivity().getSupportFragmentManager(),R.id.frame_layout2, new employerprofFragment());
                                            }
                                            employerName.setText(""+snapshot.child("fullName").getValue(String.class));
                                            employerDesignation.setText("Designation: "+snapshot.child("designation").getValue(String.class));
                                            companyName.setText(""+snapshot.child("company").getValue(String.class));
                                            if(getContext() != null) {
                                                if (snapshot.child("image").exists()) {
                                                    Glide.with(requireContext()).load(snapshot.child("image").getValue(String.class))
                                                            .into(profileImage);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

        FirebaseDatabase.getInstance().getReference("jobs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        jobList.clear();
                        if (snapshot.exists()){
                            empty.setVisibility(View.GONE);
                            jobRecyclerView.setVisibility(View.VISIBLE);
                            for (DataSnapshot ds: snapshot.getChildren()){
                                jobList.add(ds.getValue(JobModel.class));
                            }
                            jobFormAdapter.setJobList(jobList);
                        }else{
                            empty.setVisibility(View.VISIBLE);
                            jobRecyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Failed to fetch jobs: " + error.getMessage());
                    }
                });

        jobFormAdapter = new JobFormAdapter(jobList, new JobFormAdapter.OnJobActionListener() {
            @Override
            public void onEditJob(int position) {
                showJobFormDialog(position); // Open edit dialog
            }

            @Override
            public void onDeleteJob(int position) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this job?")
                        .setPositiveButton("Yes", (dialog, which) -> jobFormAdapter.deleteJob(position, requireContext()))
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }

        });

        jobRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jobRecyclerView.setAdapter(jobFormAdapter);

        createJobBtn.setOnClickListener(v -> showJobFormDialog(-1));  // Pass -1 for new job creation

        return view;
    }

    private void showJobFormDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_job_form, null);
        builder.setView(dialogView);

        EditText jobCategoryInput = dialogView.findViewById(R.id.edit_job_category);
        EditText designationInput = dialogView.findViewById(R.id.edit_designation);
        EditText skillsInput = dialogView.findViewById(R.id.edit_skills);
        EditText countryInput = dialogView.findViewById(R.id.edit_country);
        EditText cityInput = dialogView.findViewById(R.id.edit_city);
        Button saveButton = dialogView.findViewById(R.id.btn_save_job);

        AlertDialog dialog = builder.create();

        if (position != -1) {
            JobModel job = jobList.get(position);
            jobCategoryInput.setText(job.getJobCategory());
            designationInput.setText(job.getDesignation());
            skillsInput.setText(job.getSkills());
            countryInput.setText(job.getCountry());
            cityInput.setText(job.getCity());
        }

        saveButton.setOnClickListener(v -> {
            String jobCategory = jobCategoryInput.getText().toString();
            String designation = designationInput.getText().toString();
            String skills = skillsInput.getText().toString();
            String city = cityInput.getText().toString();
            String country = countryInput.getText().toString();

            if (!jobCategory.isEmpty() && !designation.isEmpty() && !skills.isEmpty() && !city.isEmpty() && !country.isEmpty()) {
                String jobID = (position == -1) ? FirebaseDatabase.getInstance().getReference("jobs").push().getKey() : jobList.get(position).getJobID();
                String employerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                JobModel updatedJob = new JobModel(jobID, employerID, companyName.getText().toString()+"", R.drawable.cc_logo4, jobCategory, designation, skills, country, city);

                // Update existing job or add a new one
                if (position != -1) {
                    jobFormAdapter.updateJob(position, updatedJob, requireContext()); // Update the job locally and in Firebase
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs");
                    databaseReference.child(jobID).setValue(updatedJob)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Job saved successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Failed to save job. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        profileListener = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        profileListener = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        profileListener = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

