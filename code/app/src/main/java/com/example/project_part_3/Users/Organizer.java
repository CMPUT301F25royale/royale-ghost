package com.example.project_part_3.Users;

public class Organizer extends User {

    public Organizer(String name, String password, String email, String phone) {
        super(name, password, email, phone);
    }

    public Organizer(String name, String password, String email) {
        super(name, password, email);
    }
}
