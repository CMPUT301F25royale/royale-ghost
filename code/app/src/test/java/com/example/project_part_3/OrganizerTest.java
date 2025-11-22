package com.example.project_part_3;

import com.example.project_part_3.Users.Organizer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrganizerTest {

    private Organizer organizer;

    @Before
    public void setUp() {
        organizer = new Organizer("testUser", "testPass", "testEmail", "1234567890");
    }

    @Test
    public void testConstructors() {
        assertEquals("testUser", organizer.getName());
        assertEquals("testPass", organizer.getPassword());
        assertEquals("testEmail", organizer.getEmail());
        assertEquals("1234567890", organizer.getPhone());
        assertEquals("Organizer", organizer.getUserType());

        Organizer organizer2 = new Organizer("testUser2", "testPass2", "testEmail2");
        assertEquals("testUser2", organizer2.getName());
        assertEquals("testPass2", organizer2.getPassword());
        assertEquals("testEmail2", organizer2.getEmail());
        assertNull(organizer2.getPhone());
        assertEquals("Organizer", organizer2.getUserType());
    }

    @Test
    public void testGetUserType() {
        assertEquals("Organizer", organizer.getUserType());
    }
}
