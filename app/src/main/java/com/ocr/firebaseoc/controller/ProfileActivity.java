package com.ocr.firebaseoc.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.controller.Auth.LoginActivity;
import com.ocr.firebaseoc.databinding.ActivityProfileBinding;


public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private int userType;

    @Override
    ActivityProfileBinding getViewBinding() {
        return ActivityProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        
        setupListeners();
        setTextUserData(currentUser);

        this.configureBottomView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
    }

    private void setupListeners(){
        // Sign out button
        binding.signOutButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setMessage("Êtes-vous sur de vouloir vous déconnecter ?")
                    .setPositiveButton("Oui",(dialogInterface, i) ->
                            AuthUI.getInstance()
                                    .signOut(this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // user is now signed out
                                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                            finish();
                                        };
                                    })
                    )
                    .setNegativeButton("Non",null)
                    .show();

        });

        // Delete button
        binding.deleteButton.setOnClickListener(view -> {

            new AlertDialog.Builder(this)
                    .setMessage(R.string.popup_message_confirmation_delete_account)
                    .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) ->
                            currentUser.delete()
                                    .addOnSuccessListener(aVoid -> {
                                                finish();
                                            }
                                    )
                    )
                    .setNegativeButton(R.string.popup_message_choice_no, null)
                    .show();
        });

        // Update button
        binding.updateButton.setOnClickListener(view -> {
            if(userType == 0)
                updateStudent(currentUser.getUid());
            else
                updateTeacher(currentUser.getUid());
        });
    }

    private void setProfilePicture(Uri profilePictureUrl){
        Glide.with(this)
                .load(profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImageView);
    }

    private void setTextUserData(FirebaseUser user){
        DocumentReference docRef = db.collection("students").document(user.getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        binding.usernameEditText.setText(document.getString("username"));
                        binding.cneTextView.setText(document.getString("cne"));
                        binding.facultyTextView.setText(document.getString("filiere") + " ("+document.getString("niveau")+")");
                        binding.phoneEditText.setText(document.getString("phoneNumber"));
                        binding.emailEditText.setText(document.getString("mailAddress"));
                        if(! TextUtils.isEmpty(document.getString("urlPicture")))
                            setProfilePicture(Uri.parse(document.getString("urlPicture")));
                        userType = 0;
                    } else {
                        DocumentReference docRef = db.collection("teachers").document(user.getUid());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        binding.usernameEditText.setText(document.getString("username"));
                                        binding.cneLabel.setText("Département");
                                        binding.cneTextView.setText(document.getString("departement"));
                                        binding.facultyLabel.setVisibility(View.GONE);
                                        binding.facultyTextView.setVisibility(View.GONE);
                                        binding.phoneEditText.setText(document.getString("phoneNumber"));
                                        binding.emailEditText.setText(document.getString("mailAddress"));
                                        if(! TextUtils.isEmpty(document.getString("urlPicture")))
                                            setProfilePicture(Uri.parse(document.getString("urlPicture")));
                                        userType = 1;
                                    } else {
                                        showSnackBar("Le compte n'existe pas");
                                    }
                                }
                            }
                        });
                    }
                } else {
                    showSnackBar("Echec de l'opération");
                }
            }
        });



    }

    private void updateStudent(String id) {
        DocumentReference documentReference = db.collection("students").document(id);

        documentReference
                .update(
                        "username", binding.usernameEditText.getText().toString(),
                        "mailAddress", binding.emailEditText.getText().toString(),
                        "phoneNumber",binding.phoneEditText.getText().toString()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showSnackBar("Opération effectuée avec succès");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackBar("Une erreur s'est produite");
                    }
                });
    }

    private void updateTeacher(String id) {
        DocumentReference documentReference = db.collection("teachers").document(id);

        documentReference
                .update(
                        "username", binding.usernameEditText.getText().toString(),
                        "mailAddress", binding.emailEditText.getText().toString(),
                        "phoneNumber",binding.phoneEditText.getText().toString()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showSnackBar("Opération effectuée avec succès");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackBar("Une erreur s'est produite");
                    }
                });
    }

    private void configureBottomView() {
        // Default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        // Add listeners
        bottomNavigationView.setOnItemSelectedListener(item ->
                updateMainFragment(item.getItemId()));
    }

    private boolean updateMainFragment(Integer integer) {
        switch (integer) {
            case R.id.nav_students:
                Intent intent3 = new Intent(getApplicationContext(), StudentsListActivity.class);
                startActivity(intent3);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_home:
                Intent intent_home = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent_home);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_teachers:
                Intent intent = new Intent(getApplicationContext(), TeachersListActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_settings:
                break;
        }
        return true;
    }

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}