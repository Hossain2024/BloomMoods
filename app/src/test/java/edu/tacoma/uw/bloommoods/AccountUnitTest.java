package edu.tacoma.uw.bloommoods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class AccountUnitTest {

    // Constructor Tests
    @Test
    public void testAccountConstructorValid() {
        Account account = new Account("test@example.com", "P@ssw0rd", true);
        assertNotNull(account);
        assertEquals("test@example.com", account.getEmail());
        assertEquals("P@ssw0rd", account.getPassword());
    }

    @Test
    public void testAccountConstructorInvalidEmail() {
        try {
            new Account("bademail@", "P@ssw0rd", true);
            fail("Account created with invalid email");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid email", e.getMessage());
        }
    }

    @Test
    public void testAccountConstructorInvalidPassword() {
        try {
            new Account("test@example.com", "pass", true);
            fail("Account created with invalid password");
        } catch (IllegalArgumentException e) {
            assertEquals("Password must be at last 6 characters long with at least 1 digit and 1 symbol", e.getMessage());
        }
    }

    // Email Tests
    @Test
    public void testSetEmailValid() {
        Account account = new Account("old@example.com", "P@ssw0rd", true);
        account.setEmail("new@example.com");
        assertEquals("new@example.com", account.getEmail());
    }

    @Test
    public void testSetEmailInvalid() {
        Account account = new Account("test@example.com", "P@ssw0rd", true);
        try {
            account.setEmail("invalidemail");
            fail("Set to an invalid email address");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid email", e.getMessage());
        }
    }

    // Password Tests
    @Test
    public void testSetPasswordValid() {
        Account account = new Account("test@example.com", "OldP@ss1", true);
        account.setPassword("NewP@ss2");
        assertEquals("NewP@ss2", account.getPassword());
    }

    @Test
    public void testSetPasswordInvalid() {
        Account account = new Account("test@example.com", "OldP@ss1", true);
        try {
            account.setPassword("short");
            fail("Set to an invalid password");
        } catch (IllegalArgumentException e) {
            assertEquals("Password must be at last 6 characters long with at least 1 digit and 1 symbol", e.getMessage());
        }
    }

    // Email Validation Tests
    @Test
    public void testIsValidEmail() {
        assertTrue(Account.isValidEmail("valid@example.com"));
        assertFalse(Account.isValidEmail("invalidemail"));
        assertFalse(Account.isValidEmail(null));
    }

    // Password Validation Tests
    @Test
    public void testIsValidPassword() {
        assertTrue(Account.isValidPassword("V@l1d123"));
        assertFalse(Account.isValidPassword("invalid")); // No digit and symbol
        assertFalse(Account.isValidPassword("short!")); // Too short
        assertFalse(Account.isValidPassword(null)); // Null check
    }
}
