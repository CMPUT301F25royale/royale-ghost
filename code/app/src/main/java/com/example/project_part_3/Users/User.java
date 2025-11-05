package com.example.project_part_3.Users;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class User {
    private String name;
    private String password;
    private String email;
    private String phone; // optional

    public User(String name, String password, String email, String phone) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = null;
    }

    public void addToDatabase(FirebaseFirestore db) {
        Map<Object, String> data = new HashMap<>();
        data.put("name", name);
        data.put("password", password);
        data.put("email", email);
        data.put("phone", phone); // may be null

        db.collection("users")
                .document(email)
                .set(data)
                .addOnSuccessListener(s -> {
                    Log.d("Success", "User was successfully added to the database");
                })
                .addOnFailureListener(f -> {
                    Log.d("Fail", "Could not add user to the database");
                });
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
    public String getPhone() { return phone; }
}
