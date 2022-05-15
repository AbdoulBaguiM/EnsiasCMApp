package com.ocr.firebaseoc.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

    public final static String DEFAULT_PROFILE_PHOTO = "https://firebasestorage.googleapis.com/v0/b/fir-oc-bc7cc.appspot.com/o/profil.png?alt=media&token=6709eab0-940d-43d5-ad35-4db54e272c77";

    // Set default profile picture
    public final static String checkProfilePicture(String url){
        if(url == null)
            url = Constants.DEFAULT_PROFILE_PHOTO;
        return url;
    }

    // Check empty strings
    public final static boolean checkIsEmpty(String string) {
        if(TextUtils.isEmpty(string))
            return true;
        return false;
    }

    // Faculty get index
    public final static int getIngenieerFacultyIndex(String faculty) {

        ArrayList<String> facultyArray = new ArrayList<String>(
                Arrays.asList("Génie logiciel","Intelligence artificielle","Smart Supply Chain and Logistics",
        "Business Intelligence et Analytics","Génie de la Data","Ingénierie Digitale pour la finance","Ingénierie en Data Science and IoT",
        "Systèmes Intelligents", "Sécurité des Systèmes d’Information"));

        return facultyArray.indexOf(faculty);
    }

    public final static int getMasterFacultyIndex(String faculty) {

        ArrayList<String> facultyArray = new ArrayList<String>(
                Arrays.asList("Internet des Objets, logiciel et analytique","Science de données et big data",
                        "Sécurité des systèmes et services","Cloud and High performance computing","Bioinformatique et modélisation des systèmes complexes")
        );

        return facultyArray.indexOf(faculty);
    }

    public final static int getDepartementIndex(String departement) {

        ArrayList<String> departementArray = new ArrayList<String>(
                Arrays.asList("Génie Logiciel","Réseaux de Communication","Informatique et aide à la décision",
                        "Ingénierie des systèmes embarqués","Web and Mobile engineering","Langues et Communication")
        );

        return departementArray.indexOf(departement);
    }

    // Level getIndex
    public final static int getIngenieerLevel(String level) {

        ArrayList<String> levelArray = new ArrayList<String>(
                Arrays.asList("I1","I2","I3")
        );

        return levelArray.indexOf(level);
    }

    public final static int getMasterLevel(String level) {

        ArrayList<String> levelArray = new ArrayList<String>(
                Arrays.asList("M1","M2")
        );

        return levelArray.indexOf(level);
    }
}
