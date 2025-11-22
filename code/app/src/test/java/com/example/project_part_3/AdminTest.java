package com.example.project_part_3;

import com.example.project_part_3.Users.Admin;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AdminTest {

    private Admin admin;

    @Before
    public void setUp() {
        admin = new Admin("testAdmin", "testPass", "testEmail", "1234567890");
    }

    @Test
    public void testConstructors() {
        assertEquals("testAdmin", admin.getName());
        assertEquals("testPass", admin.getPassword());
        assertEquals("testEmail", admin.getEmail());
        assertEquals("1234567890", admin.getPhone());
        assertEquals("Admin", admin.getUserType());

        Admin admin2 = new Admin("testAdmin2", "testPass2", "testEmail2");
        assertEquals("testAdmin2", admin2.getName());
        assertEquals("testPass2", admin2.getPassword());
        assertEquals("testEmail2", admin2.getEmail());
        assertNull(admin2.getPhone());
        assertEquals("Admin", admin2.getUserType());
    }

    @Test
    public void testGetUserType() {
        assertEquals("Admin", admin.getUserType());
    }
}
