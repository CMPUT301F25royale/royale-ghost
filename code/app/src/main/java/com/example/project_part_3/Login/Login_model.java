package com.example.project_part_3.Login;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.FirebaseFirestore;
import android.provider.Settings;
import android.content.Context;


/**
 * Model which checks if a user exists in the database and if the password is correct.
 */
public class Login_model {
    Boolean success;

    public Login_model(String email, String password) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        db.checkUser(email, password).addOnSuccessListener(user -> {
            success = user != null;

        });
    }

    public Boolean getSuccess(){
        return success;
    }

}
