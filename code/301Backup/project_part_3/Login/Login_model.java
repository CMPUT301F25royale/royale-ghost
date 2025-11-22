package com.example.project_part_3.Login;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login_model {
    Boolean success;

    public Login_model(String email, String password) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        db.checkUser(email, password).addOnSuccessListener(user -> {
            if (user != null) {
                success = true;
            } else {
                success = false;
            }

        });
    }

    public Boolean getSuccess(){
        return success;
    }

    //public User getUser(String name, String password){
        //return userDatabase.getUser(name, password);
    //}
}
