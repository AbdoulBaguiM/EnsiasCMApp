package com.ocr.firebaseoc.controller;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.adapter.TeacherAdapter;
import com.ocr.firebaseoc.model.Teacher;
import com.ocr.firebaseoc.utils.Constants;
import com.ocr.firebaseoc.utils.ItemClickSupport;

import java.io.Serializable;
import java.util.LinkedList;

public class TeachersListActivity extends AppCompatActivity implements TeacherAdapter.Listener{

    private ConstraintLayout mainLayout;
    private FirebaseFirestore db;
    private LinkedList<Teacher> teachers;
    private TeacherAdapter myAdapter;
    private RecyclerView myRecycler;
    private ProgressDialog progressDialog;
    private FloatingActionButton floatingAddButton;
    private BottomNavigationView bottomNavigationView;
    private FirebaseUser currentUser;
    private Boolean isMentor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_list);

        // Find Views by Id
        mainLayout = findViewById(R.id.mainLayout);
        floatingAddButton = findViewById(R.id.floatingAddButton);
        myRecycler = findViewById(R.id.students_recycler_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Chargement . . .");
        progressDialog.show();

        myRecycler.setVisibility(View.VISIBLE);
        myRecycler.setHasFixedSize(true);
        myRecycler.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        teachers = new LinkedList<Teacher>();

        floatingAddButton.setOnClickListener(view -> {
            Intent intent = new Intent(this,AddTeacherActivity.class);
            startActivity(intent);
        });

        this.configureOnClickRecyclerView();
        this.configureBottomView();
        this.currentUserIsStudent(new HomeActivity.UserRole() {
            @Override
            public void isStudent(boolean exist) {
                if(exist)
                    setStudentView();
                else
                    setTeacherView();
            }
        });

        this.getAllTeachers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.setSelectedItemId(R.id.nav_teachers);

        this.currentUserIsStudent(new HomeActivity.UserRole() {
            @Override
            public void isStudent(boolean exist) {
                if(exist)
                    setStudentView();
                else
                    setTeacherView();
            }
        });

        this.getAllTeachers();
    }

    private void setTeacherView() {
        isMentor = true;
        floatingAddButton.setVisibility(View.VISIBLE);
    }

    private void setStudentView() {
        isMentor = false;
    }

    private void currentUserIsStudent(HomeActivity.UserRole userRole) {

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();

        db.collection("students").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                userRole.isStudent(true);
                            }
                            else
                                userRole.isStudent(false);
                        }
                        else {
                            showSnackBar("Une erreur s'est produite");
                        }
                    }
                });
    }

    private void configureBottomView() {
        // Default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_teachers);
        // Add listeners
        bottomNavigationView.setOnItemSelectedListener(item ->
                updateMainFragment(item.getItemId()));
    }

    private boolean updateMainFragment(Integer integer) {
        switch (integer) {
            case R.id.nav_students:
                Intent intent_students_list = new Intent(getApplicationContext(), StudentsListActivity.class);
                startActivity(intent_students_list);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_home:
                Intent intent_schedule = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent_schedule);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_teachers:
                break;
            case R.id.nav_settings:
                Intent intent_settings = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent_settings);
                overridePendingTransition(0,0);
                break;
        }
        return true;
    }

    private void getAllTeachers() {

        db.collection("teachers").orderBy("username")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            teachers.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Teacher teacher = new Teacher();
                                teacher.setUid(document.getId());
                                teacher.setUsername(document.getString("username"));
                                teacher.setMailAddress(document.getString("mailAddress"));
                                teacher.setPhoneNumber(document.getString("phoneNumber"));
                                teacher.setDepartement(document.getString("departement"));
                                teacher.setMatieresEnseignes(document.getString("matieres"));
                                teacher.setUrlPicture(Constants.checkProfilePicture(document.getString("urlPicture")));

                                teachers.add(teacher);
                            }
                            myAdapter = new TeacherAdapter(teachers, Glide.with(TeachersListActivity.this), TeachersListActivity.this, isMentor);
                            myRecycler.setAdapter(myAdapter);

                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        } else {
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                            showSnackBar("Une erreur s'est produite.");
                        }

                    }
                });
    }

    // Configure item click on RecyclerView
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(myRecycler, R.layout.item_user)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Teacher teacher = myAdapter.getTeacher(position);
                        openDetailedProfileActivity(teacher);
                    }
                });
    }

    private void openDetailedProfileActivity(Teacher teacher) {
        Intent intent = new Intent(this, DetailedProfileActivity.class);
        intent.putExtra("teacher", (Serializable) teacher);
        startActivity(intent);
    }

    @Override
    public void onClickDeleteButton(int position) {
        Teacher teacher = myAdapter.getTeacher(position);

        new AlertDialog.Builder(this)
                .setMessage("Êtes vous sûr de vouloir supprimer ce compte ?")
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) ->

                    db.collection("teachers").document(teacher.getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getAllTeachers();
                                showSnackBar("Opération effectuée avec succès");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showSnackBar("Une erreur s'est produite");
                            }
                        }))
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).setAnchorView(bottomNavigationView).show();
    }
}