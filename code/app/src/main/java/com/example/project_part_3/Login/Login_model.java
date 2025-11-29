package com.example.project_part_3.Login;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.FirebaseFirestore;
import android.provider.Settings;
import android.content.Context;


public class Login_model {
    Boolean success;

    public Login_model(String email, String password) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        db.checkUser(email, password).addOnSuccessListener(user -> {
            if (user != null) {
                success = true;
                //user.addDeviceID(getCurrentDeviceID());
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

    //This probably will be moved in the future
    //public String getCurrentDeviceID(Context context){
    //    return  Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    //}
}
