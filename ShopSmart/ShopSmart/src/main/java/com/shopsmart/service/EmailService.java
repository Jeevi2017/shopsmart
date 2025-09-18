package com.shopsmart.service;

public interface EmailService {
    /**
     * Sends an email to the specified recipient with the given subject and body.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param body The content of the email.
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Sends a 2FA verification code to the user's email.
     *
     * @param to The recipient's email address.
     * @param code The 2FA verification code to send.
     * @param validityMinutes The number of minutes the code is valid for.
     */
    void send2faCode(String to, String code, int validityMinutes); // NEW: Added validityMinutes
}

