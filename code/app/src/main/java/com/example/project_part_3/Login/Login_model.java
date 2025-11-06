package com.example.project_part_3.Login;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Users.User;

public class Login_model {
    UserDatabase userDatabase = UserDatabase.getInstance();
    Boolean success;

    public Login_model(String name, String password) {
        this.success = userDatabase.checkUser(name, password);
    }

    public Boolean getSuccess(){
        return success;
    }

    public User getUser(String name, String password){
        return userDatabase.getUser(name, password);
    }
}
