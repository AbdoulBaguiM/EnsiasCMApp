package com.ocr.firebaseoc.model;


public class Teacher extends User{

    private String departement;
    private String lessons;

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getLessons() {
        return lessons;
    }

    public void setMatieresEnseignes(String matieresEnseignes) {
        this.lessons = matieresEnseignes;
    }
}
