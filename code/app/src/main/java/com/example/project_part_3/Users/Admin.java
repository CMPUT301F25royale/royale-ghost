package com.example.project_part_3.Users;

public class Admin extends User {

    public Admin(String name, String password, String email, String phone) {
        super(name, password, email, phone);
    }

    public Admin(String name, String password, String email) {
        super(name, password, email);
    }
}
