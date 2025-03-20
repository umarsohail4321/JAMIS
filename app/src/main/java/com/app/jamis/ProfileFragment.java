package com.app.jamis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.app.jamis.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;

    private DatabaseReference databaseReference;

    ArrayAdapter<CharSequence> genderAdapter;
    private ActivityResultLauncher<String> photoPickerLauncher;
    boolean isImage = false;
    Uri uri = null;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        genderAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.GenderSpinner.setAdapter(genderAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        loadProfileData();

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(requireActivity(), MainActivity.class));
                            requireActivity().finish();
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });


        // Initialize the ActivityResultLauncher inside onViewCreated
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            binding.profileImage.setImageURI(result);
                        }
                    }
                });

        binding.uploadPhotoBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Launch photo picker for Android 13+
                photoPickerLauncher.launch("image/*");
            } else {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 200);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            Uri imageUri = data.getData();
            binding.profileImage.setImageURI(imageUri);
            uri = imageUri;
            isImage = true;
        }else{
            uri = null;
            isImage = false;
        }
    }

    private void loadProfileData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String age = snapshot.child("age").getValue(String.class);
                    String qualifications = snapshot.child("qualification").getValue(String.class);
                    String companyAddress = snapshot.child("address").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String image = snapshot.child("image").getValue(String.class);


                    // Update UI with data and null checks
                    if (!TextUtils.isEmpty(name)) binding.nameInput.setText(name);
                    if (!TextUtils.isEmpty(email)) binding.fullNameInput.setText(email);
                    if (!TextUtils.isEmpty(age)) binding.ageInput.setText(age);
                    if (!TextUtils.isEmpty(qualifications)) binding.qualificationsInput.setText(qualifications);
                    if (!TextUtils.isEmpty(companyAddress)) binding.companyAddressInput.setText(companyAddress);
                    if (!TextUtils.isEmpty(gender)) {
                        int position = genderAdapter.getPosition(gender);
                        binding.GenderSpinner.setSelection(position);
                    }
                    if (!TextUtils.isEmpty(image)) Glide.with(requireContext()).load(image).into(binding.profileImage);

                } else {
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData() {

        String name = binding.nameInput.getText().toString().trim();
        String age = binding.ageInput.getText().toString().trim();
        String qualifications = binding.qualificationsInput.getText().toString().trim();
        String companyAddress = binding.companyAddressInput.getText().toString().trim();
        String gender = binding.GenderSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Please fill name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isImage){
            Utils.uploadImage(requireContext(),uri,databaseReference.child("image"));
        }
        databaseReference.child("fullName").setValue(name);
        databaseReference.child("age").setValue(age);
        databaseReference.child("qualification").setValue(qualifications);
        databaseReference.child("address").setValue(companyAddress);
        databaseReference.child("gender").setValue(gender).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

