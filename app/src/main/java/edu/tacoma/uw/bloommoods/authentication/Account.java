package edu.tacoma.uw.bloommoods.authentication;

import java.util.regex.Pattern;

public class Account {

    private String myEmail;
    private String myPassword;
    private final static int PASSWORD_LEN = 6;

    /**
     * Email validation pattern.
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );


    /**
     * Constructor to initialize email and password with validation.
     *
     * @param email    The email to set.
     * @param password The password to set.
     * @param validate Flag indicating whether to validate the email and password.
     * @throws IllegalArgumentException if the email or password is invalid.
     */
    public Account(String email, String password, Boolean validate) {
        if (validate) {
            setEmail(email);
            setPassword(password);
        } else {
            myEmail = email;
            myPassword = password;
        }

    }


    /**
     * Validates if the given input is a valid email address.
     *
     * @param email The email to validate.
     * @return {@code true} if the input is a valid email. {@code false} otherwise.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }


    /**
     * Validates if the given password is valid.
     * Valid password must be at least 6 characters long
     * with at least one digit and one symbol.
     *
     * @param password The password to validate.
     * @return {@code true} if the input is a valid password. {@code false} otherwise.
     */
    public static boolean isValidPassword(String password) {
        boolean foundDigit = false, foundSymbol = false;
        if (password == null ||
                password.length() < PASSWORD_LEN)
            return false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i)))
                foundDigit = true;
            if (!Character.isLetterOrDigit(password.charAt(i)))
                foundSymbol = true;
        }
        return foundDigit && foundSymbol;
    }

    /**
     * Sets the email after validating it.
     *
     * @param email The email to set.
     * @throws IllegalArgumentException if the email is invalid.
     */
    public void setEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.myEmail = email;
    }

    /**
     * Sets the password after validating it.
     *
     * @param password The password to set.
     * @throws IllegalArgumentException if the password is invalid.
     */
    public void setPassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at last 6 characters long with at least 1 digit and 1 symbol");
        }
        this.myPassword = password;
    }

    /**
     * Returns the email of the account.
     *
     * @return The email of the account.
     */
    public String getPassword(){
        return myPassword;
    }

    /**
     * Returns the password of the account.
     *
     * @return The password of the account.
     */
    public String getEmail(){
        return myEmail;
    }

}
