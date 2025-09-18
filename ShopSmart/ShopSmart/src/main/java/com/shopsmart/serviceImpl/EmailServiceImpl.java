package com.shopsmart.serviceImpl;

import com.shopsmart.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${mailtrap.api.url}")
    private String mailtrapApiUrl;

    @Value("${mailtrap.api.token}")
    private String mailtrapApiToken;

    @Value("${mailtrap.from.email}")
    private String fromEmail;

    @Value("${mailtrap.from.name}")
    private String fromName;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmailServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + mailtrapApiToken);

        try {
            String jsonPayload = objectMapper.writeValueAsString(
                Map.of(
                    "from", Map.of("email", fromEmail, "name", fromName),
                    "to", Collections.singletonList(Map.of("email", to)),
                    "subject", subject,
                    "text", body
                )
            );

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            restTemplate.postForEntity(mailtrapApiUrl, request, String.class);
            System.out.println("Email sent successfully via Mailtrap API to: " + to + " with subject: " + subject);

        } catch (HttpClientErrorException e) {
            System.err.println("Failed to send email via Mailtrap API to " + to + ": HTTP Error " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error sending email via Mailtrap API", e);
        } catch (Exception e) {
            System.err.println("Failed to send email via Mailtrap API to " + to + ": " + e.getMessage());
            throw new RuntimeException("Error sending email via Mailtrap API", e);
        }
    }

    @Override
    public void send2faCode(String to, String code, int validityMinutes) { // NEW: Added validityMinutes parameter
        String subject = "Your 2FA Verification Code for Ecomm App";
        String body = "Hello,\n\n"
                    + "Your two-factor authentication code is: " + code + "\n\n"
                    + "This code is valid for " + validityMinutes + " minutes. Do not share it with anyone.\n\n" // Use parameter
                    + "If you did not request this code, please ignore this email.\n\n"
                    + "Thank you,\n"
                    + "Ecomm App Team";
        sendEmail(to, subject, body);
    }
}
