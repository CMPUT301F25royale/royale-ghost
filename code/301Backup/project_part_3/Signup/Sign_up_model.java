package com.example.project_part_3.Signup;
import android.provider.ContactsContract;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Users.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;


public class Sign_up_model {

    private User user;
    private Database db;

    public Sign_up_model(String name, String password, String email, String phone, String usertype) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        this.db = new Database(firebaseFirestore);

        switch (usertype) {
            case "Entrant":
                this.user = new Entrant(name, password, email, phone);
                break;
            case "Organizer":
                this.user = new Organizer(name, password, email, phone);
                break;
            case "Admin":
                this.user = new Admin(name, password, email, phone);
                break;
            default:
                throw new IllegalArgumentException("usertype: must be Entrant, Organizer, or Admin");
        }
    }

    public Task<Boolean> registerUser() {
        return db.addUser(this.user);
    }
}
