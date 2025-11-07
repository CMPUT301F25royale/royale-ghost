package com.example.project_part_3;

import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UserTest {

    @Test
    public void addUserTest() {
        User user = new Entrant("test user", "12345pass", "coolguy69@g.com");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user.addToDatabase(db);
    }
}
