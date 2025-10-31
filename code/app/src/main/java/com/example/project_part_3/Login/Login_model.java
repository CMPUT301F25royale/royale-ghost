package com.example.project_part_3.Login;
import com.example.project_part_3.Database_functions.UserDatabase;

public class Login_model {
    UserDatabase userDatabase = UserDatabase.getInstance();
    Boolean success;

    public Login_model(String name, String password) {
        this.success = userDatabase.checkUser(name, password);
    }
    public Boolean getSuccess(){
        return success;
    }
}
