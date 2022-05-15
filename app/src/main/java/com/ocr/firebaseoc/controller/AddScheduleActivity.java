package com.ocr.firebaseoc.controller;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.databinding.ActivityAddScheduleBinding;
import com.ocr.firebaseoc.model.Schedule;
import java.util.HashMap;
import java.util.Map;

public class AddScheduleActivity extends BaseActivity<ActivityAddScheduleBinding> {
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    ActivityAddScheduleBinding getViewBinding() {
        return ActivityAddScheduleBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddScheduleActivity.this, level_ressource, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                binding.spinnerLevel.setAdapter(adapter);

                ArrayAdapter<CharSequence> adapter_faculty = ArrayAdapter.createFromResource(AddScheduleActivity.this, faculty_ressource, android.R.layout.simple_spinner_item);
                adapter_faculty.setDropDownViewResource(android.R.layout.simple_spinner_item);
                binding.spinnerFaculty.setAdapter(adapter_faculty);
            }
        });

        // Time Slot Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddScheduleActivity.this, R.array.timeslots, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        binding.spinnerTimeslot.setAdapter(adapter);

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(! fieldsEmpty()){
                    addSchedule();
                }
                else
                    showSnackBar("Veuillez remplir tous les champs");
            }
        });
    }

    private void addSchedule(){
        Schedule schedule = new Schedule();
        schedule.setLesson(binding.editTextLesson.getText().toString());
        schedule.setCycle(binding.radioGroup.getCheckedRadioButtonId() == binding.radioButtonMast.getId() ? "Master" : "Ingenieur");
        schedule.setFiliere(binding.spinnerFaculty.getSelectedItem().toString());
        schedule.setNiveau(getLevel(schedule.getCycle(),binding.spinnerLevel));
        schedule.setTimeSlot(binding.spinnerTimeslot.getSelectedItem().toString());
        schedule.setRoom(binding.editTextRoom.getText().toString());

        // Add a new document with a generated id.
        Map<String, Object> data = new HashMap<>();
        data.put("matiere",schedule.getLesson());
        data.put("filiere", schedule.getFiliere());
        data.put("niveau", schedule.getNiveau());
        data.put("creneau", schedule.getTimeSlot());
        data.put("salle",schedule.getRoom());

        db.collection("teachers").document(currentUser.getUid()).collection("schedule")
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
        if(TextUtils.isEmpty(binding.editTextLesson.getText()) ||
                TextUtils.isEmpty(binding.spinnerFaculty.getSelectedItem().toString()) ||
                TextUtils.isEmpty(binding.spinnerLevel.getSelectedItem().toString()) ||
                binding.radioGroup.getCheckedRadioButtonId() == -1 ||
                TextUtils.isEmpty(binding.editTextRoom.getText().toString())
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
}