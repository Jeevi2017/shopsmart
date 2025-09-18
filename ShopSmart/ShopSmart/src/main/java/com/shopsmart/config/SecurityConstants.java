package com.shopsmart.config;

public class SecurityConstants {

	public static final String SECRET = "ThisismysecretKeyForTheJWTTOkenAurhorizationWithH256AlgorithmWhichShouldbE256BitLongEnough"; // Replace with a strong, unique key

    public static final long EXPIRATION_TIME = 1000 * 60 * 30; 
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    // Define user roles as constants
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
}
