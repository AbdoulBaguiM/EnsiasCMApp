package com.ocr.firebaseoc.controller;


import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.ocr.firebaseoc.controller.Auth.SignUpActivity;
import com.ocr.firebaseoc.databinding.ActivityAddStudentBinding;
import com.ocr.firebaseoc.model.Student;
import com.ocr.firebaseoc.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends BaseActivity<ActivityAddStudentBinding> {

    private FirebaseFirestore db;

    @Override
    ActivityAddStudentBinding getViewBinding() {
        return ActivityAddStudentBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        setProfilePicture(Uri.parse(Constants.DEFAULT_PROFILE_PHOTO));

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

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddStudentActivity.this, level_ressource, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                binding.spinnerLevel.setAdapter(adapter);

                ArrayAdapter<CharSequence> adapter_faculty = ArrayAdapter.createFromResource(AddStudentActivity.this, faculty_ressource, android.R.layout.simple_spinner_item);
                adapter_faculty.setDropDownViewResource(android.R.layout.simple_spinner_item);
                binding.spinnerFaculty.setAdapter(adapter_faculty);
            }
        });

        binding.editImageUserImage.setOnClickListener(view -> {
            showSnackBar("Button ajouter image");
        });

        binding.addButton.setOnClickListener(view -> {
            if(!fieldsEmpty()){
                this.addStudent();
            }
            else
                showSnackBar("Veuillez remplir les champs");
        });

    }

    private void addStudent(){
        Student student = new Student();
        student.setUsername(binding.editTextUserName.getText().toString());
        student.setMailAddress(binding.editTextTextEmailAddress.getText().toString());
        student.setPhoneNumber(binding.editTextPhone.getText().toString());
        student.setCycle(binding.radioGroup.getCheckedRadioButtonId() == binding.radioButtonMast.getId() ? "Master" : "Ingenieur");
        student.setFiliere(binding.spinnerFaculty.getSelectedItem().toString());
        student.setNiveau(getLevel(student.getCycle(),binding.spinnerLevel));
        student.setCne(binding.editTextCNE.getText().toString());

        // Add a new document with a generated id.
        Map<String, Object> data = new HashMap<>();
        data.put("username",student.getUsername());
        data.put("mailAddress", student.getMailAddress());
        data.put("phoneNumber", student.getPhoneNumber());
        data.put("filiere", student.getFiliere());
        data.put("niveau", student.getNiveau());
        data.put("cycle", student.getCycle());
        data.put("cne",student.getCne());

        db.collection("students")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
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

    private boolean fieldsEmpty() {
        if(TextUtils.isEmpty(binding.editTextUserName.getText()) ||
            TextUtils.isEmpty(binding.editTextTextEmailAddress.getText()) ||
            TextUtils.isEmpty(binding.editTextPhone.getText()) ||
            TextUtils.isEmpty(binding.spinnerFaculty.getSelectedItem().toString()) ||
            TextUtils.isEmpty(binding.editTextCNE.getText()) ||
            TextUtils.isEmpty(binding.spinnerLevel.getSelectedItem().toString()) ||
            binding.radioGroup.getCheckedRadioButtonId() == -1
            )
            return true;
        return false;
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

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void setProfilePicture(Uri profilePictureUrl){
        Glide.with(this)
                .load(profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.editImageUserImage);
    }
}