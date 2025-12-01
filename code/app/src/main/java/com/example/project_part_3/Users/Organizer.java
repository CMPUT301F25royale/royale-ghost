package com.example.project_part_3.Users;

/**
 * Represents an Admin user in the system who can organize events
 * for entrants to sign up for. Organizers can also host lotteries
 * and trigger notifications to entrants.
 */
public class Organizer extends User {

    public Organizer() {
        super();
    }

    public Organizer(String name, String password, String email, String phone) {
        super(name, password, email, phone);
    }

    public Organizer(String name, String password, String email) {
        super(name, password, email);
    }

    public String getUserType(){
        return "Organizer";
    }
}
