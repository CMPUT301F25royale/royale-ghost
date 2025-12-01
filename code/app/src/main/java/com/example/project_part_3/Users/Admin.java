package com.example.project_part_3.Users;

/**
 * Represents an Admin user in the system who has permissions to delete other
 * non-admin users, events, photos, and notifications.
 */

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String name, String password, String email, String phone) {
        super(name, password, email, phone);
    }

    public Admin(String name, String password, String email) {
        super(name, password, email);
    }

    public String getUserType(){
        return "Admin";
    }
}
