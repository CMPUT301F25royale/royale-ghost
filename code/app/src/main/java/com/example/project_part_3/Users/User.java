package com.example.project_part_3.Users;

public class User {
    public String name;
    public String password;
    public String email;
    public String phone;
    public String usertype;
    public User(String name, String password, String email, String phone, String usertype) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.usertype = usertype;
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
    public String getPhone() {
        return phone;
    }
    public String getUsertype() {
        return usertype;
    }
}
