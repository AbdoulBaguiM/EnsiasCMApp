package com.ocr.firebaseoc.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.adapter.ScheduleAdapter;
import com.ocr.firebaseoc.model.Schedule;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

public class HomeActivity extends AppCompatActivity implements ScheduleAdapter.Listener{

    private FirebaseStorage storage;
    private ConstraintLayout mainLayout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView myRecycler;
    private ProgressDialog progressDialog;
    private FloatingActionButton floatingAddButton;
    private BottomNavigationView bottomNavigationView;
    private LinkedList<Schedule> schedules;
    private ScheduleAdapter myAdapter;
    private PDFView pdfView;
    private String pdfUrl;
    private FirebaseUser currentUser;
    private String scheduleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_list);

        // Find Views
        floatingAddButton = findViewById(R.id.floatingAddButton);
        myRecycler = findViewById(R.id.students_recycler_view);
        pdfView = findViewById(R.id.idPDFView);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        myRecycler = findViewById(R.id.students_recycler_view);
        mainLayout = findViewById(R.id.mainLayout);

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Chargement . . .");
        progressDialog.show();

        // Firebase connected user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        this.currentUserIsStudent(new UserRole() {
            @Override
            public void isStudent(boolean exist) {
                if(exist)
                    setStudentView();
                else
                    setTeacherView();
            }
        });

        this.configureBottomView();
    }

    interface UserRole {
        void isStudent(boolean exist);
    }

    private void currentUserIsStudent(UserRole userRole) {

        db = FirebaseFirestore.getInstance();

        db.collection("students").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                scheduleName = document.getString("filiere").replaceAll("\\s","")+document.getString("niveau");
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

    private void setTeacherView() {
        myRecycler.setVisibility(View.VISIBLE);
        floatingAddButton.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        myRecycler.setHasFixedSize(true);
        myRecycler.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        schedules = new LinkedList<Schedule>();

        this.getMySchedule();

        floatingAddButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),AddScheduleActivity.class);
            startActivity(intent);
        });
    }

    private void setStudentView() {

        // [START storage_field_initialization]
        storage = FirebaseStorage.getInstance();
        // [END storage_field_initialization]

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        StorageReference schedule = storageRef.child("schedule/"+scheduleName+".pdf");

        // PDFView
        schedule.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                pdfUrl = uri.toString();
                new RetrivePDFfromUrl().execute(pdfUrl);
                progressDialog.dismiss();
            }
        });
    }


    private void getMySchedule() {
        db.collection("teachers").document(currentUser.getUid())
                .collection("schedule")
                .orderBy("creneau")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            schedules.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Schedule schedule = new Schedule();
                                schedule.setId(document.getId());
                                schedule.setTimeSlot(document.getString("creneau"));
                                schedule.setFiliere(document.getString("filiere"));
                                schedule.setLesson(document.getString("matiere"));
                                schedule.setNiveau(document.getString("niveau"));
                                schedule.setRoom(document.getString("salle"));
                                schedules.add(schedule);
                            }
                            myAdapter = new ScheduleAdapter(schedules, HomeActivity.this);
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

    @Override
    public void onClickDeleteButton(int position) {
        Schedule schedule = myAdapter.getSchedule(position);

        new AlertDialog.Builder(this)
                .setMessage("Êtes vous sûr de vouloir supprimer ce compte ?")
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) ->

                        db.collection("teachers").document(currentUser.getUid())
                                .collection("schedule")
                                .document(schedule.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getMySchedule();
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

    private void configureBottomView() {
        // Default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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
                break;
            case R.id.nav_teachers:
                Intent intent = new Intent(getApplicationContext(), TeachersListActivity.class);
                startActivity(intent);
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

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).setAnchorView(bottomNavigationView).show();
    }

    // Create an async task class for loading pdf file from URL.
    class RetrivePDFfromUrl extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            // we are using inputstream
            // for getting out PDF.
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                // below is the step where we are
                // creating our connection.
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            // after the execution of our async
            // task we are loading our pdf in our pdf view.
            pdfView.fromStream(inputStream).load();
        }
    }
}