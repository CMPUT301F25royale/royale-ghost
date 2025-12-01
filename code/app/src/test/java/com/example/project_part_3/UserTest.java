package com.example.project_part_3;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

import com.example.project_part_3.Users.User;

/**
 * Unit tests for {@link User}.
 *
 * Notes:
 * - Pure JVM tests
 */
public class UserTest {

    @Test
    public void defaultConstructor_initializesNulls() {
        User u = new User();
        assertNull(u.getName());
        assertNull(u.getPassword());
        assertNull(u.getEmail());
        assertNull(u.getPhone());
        assertNull(u.getUserType());
    }

    @Test
    public void constructorWithPhone_setsAllFieldsExceptUserType() {
        // Matches User(String name, String password, String email, String phone)
        User u = new User("Liam", "password123", "liam@example.com", "555-1234");
        assertEquals("Liam", u.getName());
        assertEquals("password123", u.getPassword());
        assertEquals("liam@example.com", u.getEmail());
        assertEquals("555-1234", u.getPhone());
        assertNull(u.getUserType());
    }

    @Test
    public void constructorWithoutPhone_setsPhoneNull() {
        // Matches User(String name, String password, String email)
        User u = new User("Liam", "secret", "bob@example.com");
        assertEquals("Liam", u.getName());
        assertEquals("secret", u.getPassword());
        assertEquals("bob@example.com", u.getEmail());
        assertNull(u.getPhone());
        assertNull(u.getUserType());
    }

    @Test
    public void altConstructor_setsUserType() {
        // FIXED: Removed the 6th 'null' argument.
        // Matches User(String name, String email, String phone, String password, String userType)
        User u = new User("Jane Doe", "jane@example.com", "555-2222", "password456", "organizer");

        assertEquals("Jane Doe", u.getName());
        assertEquals("password456", u.getPassword());
        assertEquals("jane@example.com", u.getEmail());
        assertEquals("555-2222", u.getPhone());
        assertEquals("organizer", u.getUserType());
    }

    @Test
    public void setters_updateValues() {
        User u = new User();
        u.setName("Hugus");
        u.setPassword("p@ss");
        u.setEmail("hugus@example.com");
        u.setPhone("555-3333");
        u.setUserType("entrant");

        assertEquals("Hugus", u.getName());
        assertEquals("p@ss", u.getPassword());
        assertEquals("hugus@example.com", u.getEmail());
        assertEquals("555-3333", u.getPhone());
        assertEquals("entrant", u.getUserType());
    }

    @Test
    public void setters_acceptNulls() {
        User u = new User("el chapo", "pw", "chapo@example.com", "555-4444");
        u.setName(null);
        u.setPassword(null);
        u.setEmail(null);
        u.setPhone(null);
        u.setUserType(null);

        assertNull(u.getName());
        assertNull(u.getPassword());
        assertNull(u.getEmail());
        assertNull(u.getPhone());
        assertNull(u.getUserType());
    }

    @Test
    public void serializable_roundTrip_preservesState() throws Exception {
        User original = new User("Zoe", "zpw", "zoe@example.com", "555-9999");
        original.setUserType("admin");

        // Serialize to bytes
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
            oos.flush();
            bytes = bos.toByteArray();
        }

        // Deserialize
        User copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            copy = (User) ois.readObject();
        }

        assertNotNull(copy);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getPassword(), copy.getPassword());
        assertEquals(original.getEmail(), copy.getEmail());
        assertEquals(original.getPhone(), copy.getPhone());
        assertEquals(original.getUserType(), copy.getUserType());
    }
}