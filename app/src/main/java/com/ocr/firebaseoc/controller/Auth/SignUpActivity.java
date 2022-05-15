package com.ocr.firebaseoc.controller.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.controller.HomeActivity;
import com.ocr.firebaseoc.model.Student;
import com.ocr.firebaseoc.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private TextInputEditText username, password, email, phone, cne;
    private RadioGroup cycle;
    private RadioButton cycleIng, cycleMast;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner levelSpiner, facultySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mainLayout = findViewById(R.id.mainLayout);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.editTextUserName);
        password = findViewById(R.id.editTextPassword);
        email = findViewById(R.id.editTextEmailAddress);
        phone = findViewById(R.id.editTextPhone);
        cne = findViewById(R.id.editTextCNE);
        cycle = findViewById(R.id.radioGroup);
        cycleIng = findViewById(R.id.radioButtonIng);
        cycleMast = findViewById(R.id.radioButtonMast);

        // Spinners
        levelSpiner = findViewById(R.id.spinner_level);
        facultySpinner = findViewById(R.id.spinner_faculty);

        cycle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int level_ressource = 0;
                int faculty_ressource = 0;

                if(checkedId == cycleMast.getId()){
                    level_ressource = R.array.level_master;
                    faculty_ressource = R.array.faculty_master;
                }

                else if (checkedId == cycleIng.getId()){
                    level_ressource = R.array.level_ingenieur;
                    faculty_ressource = R.array.faculty_ingenieur;
                }

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(SignUpActivity.this, level_ressource, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                levelSpiner.setAdapter(adapter);

                ArrayAdapter<CharSequence> adapter_faculty = ArrayAdapter.createFromResource(SignUpActivity.this, faculty_ressource, android.R.layout.simple_spinner_item);
                adapter_faculty.setDropDownViewResource(android.R.layout.simple_spinner_item);
                facultySpinner.setAdapter(adapter_faculty);
            }
        });

        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constants.checkIsEmpty(username.getText().toString()) || Constants.checkIsEmpty(password.getText().toString()) || Constants.checkIsEmpty(email.getText().toString()) || Constants.checkIsEmpty(phone.getText().toString()) || Constants.checkIsEmpty(facultySpinner.getSelectedItem().toString()) || Constants.checkIsEmpty(cne.getText().toString()) || Constants.checkIsEmpty(levelSpiner.getSelectedItem().toString()) || cycle.getCheckedRadioButtonId() == -1)
                    showSnackBar("Veuillez renseigner vos informations");
                else
                    createNewUser();
            }
        });
    }

    private void createNewUser() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            addNewStudent(user);
                            // Sign in success, update UI
                            startHomeActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            showSnackBar("Une erreur s'est produite, veuillez réessayer (creation)");
                        }
                    }
                });
    }

    private void addNewStudent(FirebaseUser user){
        Student student = new Student();
        student.setUsername(username.getText().toString());
        student.setMailAddress(email.getText().toString());
        student.setPhoneNumber(phone.getText().toString());
        student.setCycle(cycle.getCheckedRadioButtonId() == cycleMast.getId() ? "Master" : "Ingenieur");
        student.setFiliere(facultySpinner.getSelectedItem().toString());
        student.setNiveau(getLevel(student.getCycle(),levelSpiner));
        student.setCne(cne.getText().toString());

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
                .document(user.getUid())
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackBar("Une erreur s'est produite, veuillez réessayer (ajout)");
                    }
                });
    }

    private void startHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
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
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}