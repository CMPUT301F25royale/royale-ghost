package com.example.project_part_3.Users;

import java.io.Serializable;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import android.util.Log;


public class User implements Serializable {
    private String name;
    private String password;
    private String email;
    private String phone; // optional
    private String userType;
    private String profilePicUrl;
    private HashSet<String> deviceIDs;



    public User() {
        // required for firebase
    }

    public User(String name, String password, String email, String phone) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.profilePicUrl = null;
    }

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = null;
        this.profilePicUrl = null;
    }

    public User(String janeSmith, String mail, String number, String password456, String organizer) {
        this.name = janeSmith;
        this.password = password456;
        this.email = mail;
        this.phone = number;
        this.userType = organizer;
        this.profilePicUrl = null;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public HashSet<String> getDeviceIDs() {return deviceIDs;}

    public void addDeviceID(String deviceID) {this.deviceIDs.add(deviceID);}
}
