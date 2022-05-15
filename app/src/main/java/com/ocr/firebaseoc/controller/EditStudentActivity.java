package com.ocr.firebaseoc.controller;


import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.databinding.ActivityAddStudentBinding;
import com.ocr.firebaseoc.model.Student;
import com.ocr.firebaseoc.utils.Constants;

public class EditStudentActivity extends BaseActivity<ActivityAddStudentBinding> {

    private FirebaseFirestore db;

    @Override
    ActivityAddStudentBinding getViewBinding() {
        return ActivityAddStudentBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        Student student = new Student();
        student = (Student) getIntent().getSerializableExtra("student");

        binding.editTextUserName.setText(student.getUsername());
        binding.editTextTextEmailAddress.setText(student.getMailAddress());
        binding.editTextCNE.setText(student.getCne());
        binding.editTextPhone.setText(student.getPhoneNumber());

        // Spinners
        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int level_ressource = 0;
                int faculty_ressource = 0;

                if(checkedId == binding.radioButtonMast.getId()){
                    level_ressource = R.array.level_master;
                    faculty_ressource = R.array.faculty_master;
                }

                else if (checkedId == binding.radioButtonIng.getId()){
                    level_ressource = R.array.level_ingenieur;
                    faculty_ressource = R.array.faculty_ingenieur;
                }

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditStudentActivity.this, level_ressource, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                binding.spinnerLevel.setAdapter(adapter);

                ArrayAdapter<CharSequence> adapter_faculty = ArrayAdapter.createFromResource(EditStudentActivity.this, faculty_ressource, android.R.layout.simple_spinner_item);
                adapter_faculty.setDropDownViewResource(android.R.layout.simple_spinner_item);
                binding.spinnerFaculty.setAdapter(adapter_faculty);
            }
        });

        if (student.getCycle().equals("Master")) {
            binding.radioButtonMast.setChecked(true);
            binding.spinnerFaculty.setSelection(Constants.getMasterFacultyIndex(student.getFiliere()));
            binding.spinnerLevel.setSelection(Constants.getMasterLevel(student.getNiveau()));
        } else {
            binding.radioButtonIng.setChecked(true);
            binding.spinnerFaculty.setSelection(Constants.getIngenieerFacultyIndex(student.getFiliere()));
            binding.spinnerLevel.setSelection(Constants.getIngenieerLevel(student.getNiveau()));
        }

        if(student.getUrlPicture() != null){
            setProfilePicture(Uri.parse(student.getUrlPicture()));
        }

        Student finalStudent = student;
        binding.addButton.setOnClickListener(view -> {
            updateStudent(finalStudent.getUid());
        });
    }

    private void updateStudent(String id) {
        DocumentReference documentReference = db.collection("students").document(id);

        documentReference
            .update(
                "username", binding.editTextUserName.getText().toString(),
                "mailAddress", binding.editTextTextEmailAddress.getText().toString(),
                "phoneNumber",binding.editTextPhone.getText().toString(),
                "filiere",binding.spinnerFaculty.getSelectedItem().toString(),
                "cne",binding.editTextCNE.getText().toString(),
                "cycle",getStudentCycle(),
                "niveau",getLevel(getStudentCycle(),binding.spinnerLevel))
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    showSnackBar("Opération effecetuée avec succès");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showSnackBar("Une erreur s'est produite");
                }
            });
        NavUtils.navigateUpFromSameTask(this);
    }

    private String getStudentCycle() {
        if(binding.radioGroup.getCheckedRadioButtonId() == binding.radioButtonMast.getId())
            return "Master";
        return "Ingénieur";
    }

    private String getLevel(String cycle, Spinner levelSpiner) {
        char realCycle = cycle.charAt(0);
        String result = null;

        if(levelSpiner.getSelectedItemId() == 0)
            result = realCycle+"1";
        else if(levelSpiner.getSelectedItemId() == 1)
            result = realCycle+"2";
        else if(levelSpiner.getSelectedItemId() == 2)
            result = realCycle+"3";

        return result;
    }

    private void setProfilePicture(Uri profilePictureUrl){
        Glide.with(this)
                .load(profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.editImageUserImage);
    }

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}