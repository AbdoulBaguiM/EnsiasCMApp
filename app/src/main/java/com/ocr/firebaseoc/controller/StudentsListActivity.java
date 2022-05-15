package com.ocr.firebaseoc.controller;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.ocr.firebaseoc.adapter.StudentAdapter;
import com.ocr.firebaseoc.model.Student;
import com.ocr.firebaseoc.utils.Constants;
import com.ocr.firebaseoc.utils.ItemClickSupport;


import java.io.Serializable;
import java.util.ArrayList;

public class StudentsListActivity extends AppCompatActivity implements StudentAdapter.Listener{

    private ConstraintLayout mainLayout;
    private FirebaseFirestore db;
    private StudentAdapter myStudentAdapter;
    private ArrayList<Student> students;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private FloatingActionButton floatingAddButton;
    private BottomNavigationView bottomNavigationView;
    private FirebaseUser currentUser;
    private Boolean isMentor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_list);

        // Find Views by ID
        mainLayout = findViewById(R.id.mainLayout);
        floatingAddButton = findViewById(R.id.floatingAddButton);
        recyclerView = findViewById(R.id.students_recycler_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Chargement . . .");
        progressDialog.show();

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        students = new ArrayList<Student>();

        floatingAddButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddStudentActivity.class);
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

        this.getAllStudents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_students);

        this.currentUserIsStudent(new HomeActivity.UserRole() {
            @Override
            public void isStudent(boolean exist) {
                if(exist)
                    setStudentView();
                else
                    setTeacherView();
            }
        });

        this.getAllStudents();
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
        bottomNavigationView.setSelectedItemId(R.id.nav_students);
        // Add listeners
        bottomNavigationView.setOnItemSelectedListener(item ->
                updateMainFragment(item.getItemId()));
    }

    private boolean updateMainFragment(Integer integer) {
        switch (integer) {
            case R.id.nav_students:
                break;
            case R.id.nav_home:
                Intent intent_schedule = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent_schedule);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_teachers:
                Intent intent_teachers_list = new Intent(getApplicationContext(), TeachersListActivity.class);
                startActivity(intent_teachers_list);
                overridePendingTransition(0,0);
                break;
            case R.id.nav_settings:
                Intent intent_settings = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent_settings);
                overridePendingTransition(0,0);
                break;
        }
        return true;
    }

    private void getAllStudents() {

        db.collection("students").orderBy("username")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            students.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Student student = new Student();
                                student.setUid(document.getId());
                                student.setUsername(document.getString("username"));
                                student.setMailAddress(document.getString("mailAddress"));
                                student.setPhoneNumber(document.getString("phoneNumber"));
                                student.setCne(document.getString("cne"));
                                student.setCycle(document.getString("cycle"));
                                student.setFiliere(document.getString("filiere"));
                                student.setNiveau(document.getString("niveau"));
                                student.setUrlPicture(Constants.checkProfilePicture(document.getString("urlPicture")));

                                students.add(student);
                            }
                        } else {
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                            showSnackBar("Une erreur s'est produite.");
                        }

                        myStudentAdapter = new StudentAdapter(students, Glide.with(StudentsListActivity.this), StudentsListActivity.this, isMentor);
                        recyclerView.setAdapter(myStudentAdapter);

                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });
    }

    // Configure item click on RecyclerView
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(recyclerView, R.layout.item_user)
            .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Student student = myStudentAdapter.getStudent(position);
                    openDetailedProfileActivity(student);
                }
            });
    }

    private void openDetailedProfileActivity(Student student) {
        Intent intent = new Intent(this, DetailedProfileActivity.class);
        intent.putExtra("student", (Serializable) student);
        startActivity(intent);
    }

    @Override
    public void onClickDeleteButton(int position) {
        Student student = myStudentAdapter.getStudent(position);

        new AlertDialog.Builder(this)
                .setMessage("Êtes vous sûr de vouloir supprimer ce compte ?")
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) ->

                    db.collection("students").document(student.getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getAllStudents();
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