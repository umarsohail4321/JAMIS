package com.app.jamis;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ButtonFragment extends Fragment {

    private Button EmployerButton, EmployeeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_button, container, false);

        EmployerButton = view.findViewById(R.id.EmployerButton);
       EmployeeButton = view.findViewById(R.id.EmployeeButton);

        EmployerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent employerIntent = new Intent(getActivity(), Login.class);
                employerIntent.putExtra("type", "Employer");
                startActivity(employerIntent);
            }
        });

        EmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent employeeIntent = new Intent(getActivity(), Login.class);
                employeeIntent.putExtra("type", "Employee");
                startActivity(employeeIntent);
            }
        });

        return view;
    }
}