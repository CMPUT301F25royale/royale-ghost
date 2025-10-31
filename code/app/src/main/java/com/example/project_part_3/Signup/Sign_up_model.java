package com.example.project_part_3.Signup;
import com.example.project_part_3.Database_functions.UserDatabase;


public class Sign_up_model{
    Boolean success;
    UserDatabase userDatabase = UserDatabase.getInstance();

    public Sign_up_model(String name, String password, String email, String phone, String usertype) {
      success = userDatabase.addUser(name, password, email, phone, usertype);
    }

    public Boolean getSuccess(){
        return success;
    }

}
