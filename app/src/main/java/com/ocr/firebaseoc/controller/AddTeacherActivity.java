package com.ocr.firebaseoc.controller;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.databinding.ActivityAddTeacherBinding;
import com.ocr.firebaseoc.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class AddTeacherActivity extends BaseActivity<ActivityAddTeacherBinding> {

    private FirebaseFirestore db;

    @Override
    ActivityAddTeacherBinding getViewBinding() {
        return ActivityAddTeacherBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        setProfilePicture(Uri.parse(Constants.DEFAULT_PROFILE_PHOTO));

        binding.editImageUserImage.setOnClickListener(view -> {
            showSnackBar("Button ajouter image");
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddTeacherActivity.this, R.array.departements, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        binding.departementSpinner.setAdapter(adapter);

        binding.addButton.setOnClickListener(view -> {
            if( ! checkRequiredFields())
                showSnackBar("Veuillez remplir tous les champs");
            else
                this.addTeacher();
        });
    }

    private boolean checkRequiredFields() {
        if(TextUtils.isEmpty(binding.editTextUserName.getText()) ||
            binding.departementSpinner.getSelectedItemId() == -1)

            return false;

        return true;
    }

    private void addTeacher() {
        // Add a new document with a generated id.
        Map<String, Object> data = new HashMap<>();
        data.put("username",binding.editTextUserName.getText().toString());
        data.put("mailAddress", binding.editTextTextEmailAddress.getText().toString());
        data.put("phoneNumber", binding.editTextPhone.getText().toString());
        data.put("departement", binding.departementSpinner.getSelectedItem().toString());
        data.put("matieres", binding.editTextLessons.getText().toString());

        db.collection("teachers")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showSnackBar("Opération effectuée avec succès" );
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