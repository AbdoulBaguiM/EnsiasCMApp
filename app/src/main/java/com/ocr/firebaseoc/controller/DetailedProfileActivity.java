package com.ocr.firebaseoc.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.adapter.StudentAdapter;
import com.ocr.firebaseoc.model.Student;
import com.ocr.firebaseoc.model.Teacher;
import com.ocr.firebaseoc.model.User;

import java.io.Serializable;

public class DetailedProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView userphoto;
    private TextView username,mail,phone,departement,lessons,extras;
    private ImageButton emailSendButton,phoneCallButton,smsSendButton;
    private LinearLayout mainLayout;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_profile);

        db = FirebaseFirestore.getInstance();
        mainLayout = findViewById(R.id.mainLayout);
        userphoto = findViewById(R.id.user_photo);
        username = findViewById(R.id.user_name);
        mail = findViewById(R.id.user_mail);
        phone = findViewById(R.id.user_phone);
        departement = findViewById(R.id.user_departement);
        lessons = findViewById(R.id.user_lessons);
        extras = findViewById(R.id.user_extras);
        emailSendButton = findViewById(R.id.emailSend);
        phoneCallButton = findViewById(R.id.phoneCall);
        smsSendButton = findViewById(R.id.smsSend);


        Intent intent = getIntent();

        if (intent.hasExtra("teacher")) {
            setTeacherDetailedProfile(intent);
        } else if(intent.hasExtra("student")){
            setStudentDetailedProfile(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.activity_detailedprofile_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem editButton = menu.findItem(R.id.editButton);
        MenuItem deleteButton = menu.findItem(R.id.deleteButton);

        this.currentUserIsStudent(new HomeActivity.UserRole() {
            @Override
            public void isStudent(boolean exist) {
                if(exist){

                }
                else{
                    editButton.setVisible(true);
                    deleteButton.setVisible(true);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle actions on items selection
        switch (item.getItemId()) {
            case R.id.deleteButton : this.deleteAccount();return true;
            case R.id.editButton : this.editAccount();return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void editAccount() {

        if (getIntent().hasExtra("teacher")) {
            Intent intent = new Intent(this,EditTeacherActivity.class);
            Teacher teacher = (Teacher) getIntent().getSerializableExtra("teacher");
            intent.putExtra("teacher", (Serializable) teacher);
            startActivity(intent);
        } else if(getIntent().hasExtra("student")){
            Intent intent = new Intent(this,EditStudentActivity.class);
            Student student = (Student) getIntent().getSerializableExtra("student");
            intent.putExtra("student",(Serializable) student);
            startActivity(intent);
        }
    }

    private void deleteAccount() {
        User user = new User();
        String collectionPath = null;

        if (getIntent().hasExtra("teacher")) {
            user = (Teacher) getIntent().getSerializableExtra("teacher");
            collectionPath ="teachers";
        } else if(getIntent().hasExtra("student")){
            user = (Student) getIntent().getSerializableExtra("student");
            collectionPath = "students";
        }

        User finalUser = user;
        String finalCollectionPath = collectionPath;

        new AlertDialog.Builder(this)
                    .setMessage("Êtes vous sûr de vouloir supprimer ce compte ?")
                    .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) ->

                            db.collection(finalCollectionPath).document(finalUser.getUid())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            NavUtils.navigateUpFromSameTask(DetailedProfileActivity.this);
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

    private void setProfilePicture(Uri profilePictureUrl){
        Glide.with(this)
                .load(profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(userphoto);
    }

    private void setStudentDetailedProfile(Intent intent) {

        Student student = (Student) intent.getSerializableExtra("student");

        if(student.getUrlPicture() != null){
            setProfilePicture(Uri.parse(student.getUrlPicture()));
        }

        username.setText(student.getUsername());
        mail.setText(student.getMailAddress());
        phone.setText(student.getPhoneNumber());
        departement.setText("Cycle : "+student.getCycle() +"\n"+ "Filière : " +student.getFiliere());
        extras.setText("Niveau : "+student.getNiveau() +"\n"+ "CNE : "+student.getCne());

        configureDialButtons(student);
    }

    private void setTeacherDetailedProfile(Intent intent) {

        Teacher teacher = (Teacher) intent.getSerializableExtra("teacher");

        if(teacher.getUrlPicture() != null){
            setProfilePicture(Uri.parse(teacher.getUrlPicture()));
        }

        username.setText("Pr. " +teacher.getUsername());
        departement.setText("Département : "+teacher.getDepartement());
        lessons.setText(teacher.getLessons());
        extras.setText("Matières enseignées : ");

        if( ! TextUtils.isEmpty(teacher.getPhoneNumber())) {
            phone.setText(teacher.getPhoneNumber());
            phone.setVisibility(View.VISIBLE);
        }


        if( ! TextUtils.isEmpty(teacher.getMailAddress())) {
            mail.setText(teacher.getMailAddress());
            mail.setVisibility(View.VISIBLE);
        }

        configureDialButtons(teacher);
    }

    public void composeEmail(String address) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" +address)); // only email apps should handle this
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void composeSmsMessage(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("smsto:" + phoneNumber));  // This ensures only SMS apps respond
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void configureDialButtons(Object object){
        if(object instanceof User){
            if( ! TextUtils.isEmpty(((User) object).getMailAddress())){
                emailSendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        composeEmail(((User) object).getMailAddress());
                    }
                });
                emailSendButton.setVisibility(View.VISIBLE);
            }

            if( ! TextUtils.isEmpty(((User) object).getPhoneNumber())){

                phoneCallButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialPhoneNumber(((User) object).getPhoneNumber());
                    }
                });

                smsSendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        composeSmsMessage(((User) object).getPhoneNumber());
                    }
                });

                smsSendButton.setVisibility(View.VISIBLE);
                phoneCallButton.setVisibility(View.VISIBLE);
            }

        }

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

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }

}