package com.example.project_part_3;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

import com.example.project_part_3.Users.User;

/**
 * Unit tests for {@link User}.
 *
 * Notes:
 * - Pure JVM tests (no Android deps).
 * - Uses JUnit4 (matches your Gradle setup).
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
        User u = new User("Alice", "pass123", "alice@example.com", "555-1111");
        assertEquals("Alice", u.getName());
        assertEquals("pass123", u.getPassword());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("555-1111", u.getPhone());
        assertNull(u.getUserType());
    }

    @Test
    public void constructorWithoutPhone_setsPhoneNull() {
        User u = new User("Bob", "secret", "bob@example.com");
        assertEquals("Bob", u.getName());
        assertEquals("secret", u.getPassword());
        assertEquals("bob@example.com", u.getEmail());
        assertNull(u.getPhone());
        assertNull(u.getUserType());
    }

    @Test
    public void altConstructor_setsUserType() {
        // Signature: (String name, String email, String phone, String password, String userType, Object o)
        User u = new User("Jane Smith", "jane@example.com", "555-2222", "password456", "organizer", null);
        assertEquals("Jane Smith", u.getName());
        assertEquals("password456", u.getPassword());
        assertEquals("jane@example.com", u.getEmail());
        assertEquals("555-2222", u.getPhone());
        assertEquals("organizer", u.getUserType());
    }

    @Test
    public void setters_updateValues() {
        User u = new User();
        u.setName("Charlie");
        u.setPassword("p@ss");
        u.setEmail("charlie@example.com");
        u.setPhone("555-3333");
        u.setUserType("entrant");

        assertEquals("Charlie", u.getName());
        assertEquals("p@ss", u.getPassword());
        assertEquals("charlie@example.com", u.getEmail());
        assertEquals("555-3333", u.getPhone());
        assertEquals("entrant", u.getUserType());
    }

    @Test
    public void setters_acceptNulls() {
        User u = new User("Nina", "pw", "nina@example.com", "555-4444");
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
