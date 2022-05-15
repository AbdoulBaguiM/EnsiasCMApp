package com.ocr.firebaseoc.controller;


import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

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
import com.ocr.firebaseoc.databinding.ActivityAddTeacherBinding;
import com.ocr.firebaseoc.model.Teacher;
import com.ocr.firebaseoc.utils.Constants;

public class EditTeacherActivity extends BaseActivity<ActivityAddTeacherBinding> {

    private FirebaseFirestore db;

    @Override
    ActivityAddTeacherBinding getViewBinding() {
        return ActivityAddTeacherBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        Teacher teacher = new Teacher();
        teacher = (Teacher) getIntent().getSerializableExtra("teacher");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditTeacherActivity.this, R.array.departements, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        binding.departementSpinner.setAdapter(adapter);

        binding.editTextUserName.setText(teacher.getUsername());
        binding.editTextTextEmailAddress.setText(teacher.getMailAddress());
        binding.editTextPhone.setText(teacher.getPhoneNumber());
        binding.departementSpinner.setSelection(Constants.getDepartementIndex(teacher.getDepartement()));
        binding.editTextLessons.setText(teacher.getLessons());

        if(teacher.getUrlPicture() != null){
            setProfilePicture(Uri.parse(teacher.getUrlPicture()));
        }

        Teacher finalTeacher = teacher;
        binding.addButton.setOnClickListener(view -> {
            updateTeacher(finalTeacher.getUid());
        });
    }

    private void updateTeacher(String id) {
        DocumentReference documentReference = db.collection("teachers").document(id);

        documentReference
                .update(
                        "username", binding.editTextUserName.getText().toString(),
                        "mailAddress", binding.editTextTextEmailAddress.getText().toString(),
                        "phoneNumber",binding.editTextPhone.getText().toString(),
                        "departement",binding.departementSpinner.getSelectedItem().toString(),
                        "matieres", binding.editTextLessons.getText().toString()
                        )
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