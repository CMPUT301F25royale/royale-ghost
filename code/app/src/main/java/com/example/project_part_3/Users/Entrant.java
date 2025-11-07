package com.example.project_part_3.Users;

public class Entrant extends User {
    // TODO: add new attributes

    public Entrant(String name, String password, String email, String phone) {
        super(name, password, email, phone);

    }

    public Entrant(String name, String password, String email) {
        super(name, password, email);
    }

    public String getUserType(){
        return "Entrant";
    }
}
