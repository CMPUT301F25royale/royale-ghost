package com.example.project_part_3.Users;

public abstract class User {
    public int userID;

    public String name;
    public String password;
    public String email;
    public String phone; // optional

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
