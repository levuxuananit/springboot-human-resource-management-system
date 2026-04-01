package com.r2s.core.config;

public class SecurityConstants {
    //[!] Token properties
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final int EXPIRATION_TIME = 2*60*60*1000; //2h
    public static final String SECRET = "bXlzZWNyZXRrZXlteXNlY3JldGtleW15c2VjcmV0a2V5MTYwOTIwMjYwMA==";

    // [!] DTO Validation for username
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String USERNAME_SIZE = "Username must be between 3 and 20 characters";
    public static final String USERNAME_INVALID = "Username can only contain letters, numbers, dots, and underscores";

    // [!] DTO Validation for email
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Invalid email format";

    // [!] DTO Validation for password
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_SIZE = "Password must be at least 8 characters";
    public static final String PASSWORD_COMPLEXITY = "Password must contain at least one uppercase, one lowercase, one digit, and one special character";

    // [!] DTO Validation for other fields
    public static final String FULLNAME_REQUIRED = "Full name is required";
    public static final String PHONE_INVALID = "Phone number must be between 10 and 11 digits";
}
